package io.spine.publishing.github

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.FileReader
import java.nio.file.Path
import java.security.KeyPair
import java.security.PrivateKey
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.time.Instant
import java.time.Instant.now
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*

/**
 * A JWT that can be used to authorize [GitHubApiRequest]s.
 *
 * @param value the string value of the JWT
 * @param expirationTime the time after which the JWT is no longer usable
 */
data class GitHubJwt(val value: String, val expirationTime: Instant) {

    val isExpired get() = now().isAfter(expirationTime)
}

class SignedJwts(private val privateKeyPath: Path, private val gitHubAppId: AppId) : JwtFactory {

    /**
     * Generates a GitHub JWT, signing it using a private key from  a `.pem` file
     * located by the specified path.
     *
     * The JWT created by this method expires in 10 minutes. After expiration, it no longer
     * can authorize [GitHubApiRequest]s.
     */
    override fun newJwt(): GitHubJwt {
        Security.addProvider(BouncyCastleProvider())
        val pemParser = PEMParser(FileReader(privateKeyPath.toFile()))
        val converter = JcaPEMKeyConverter().setProvider("BC")

        val pemObject: Any = pemParser.readObject()
        val keyPair: KeyPair = converter.getKeyPair(pemObject as PEMKeyPair)
        val privateKey: PrivateKey = keyPair.private

        val now = now()
        val expirationTime = now.plus(10, MINUTES)
        val jwt = JWT.create()
                .withIssuer(gitHubAppId)
                .withIssuedAt(Date.from(now))
                // GitHub token lifetime cannot exceed 10 minutes.
                .withExpiresAt(Date.from(expirationTime))
                // We only have the private key from the GitHub App.
                .sign(Algorithm.RSA256(null, privateKey as RSAPrivateKey))
        return GitHubJwt(jwt, expirationTime)
    }

}

interface JwtFactory {

    fun newJwt(): GitHubJwt
}

/** The ID of the GitHub App. */
typealias AppId = String

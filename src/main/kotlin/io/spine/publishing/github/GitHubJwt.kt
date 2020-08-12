package io.spine.publishing.github

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.spine.publishing.github.GitHubJwt.Companion.generate
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
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*

/**
 * A JWT that can be used to authorize [GitHubApiRequest]s.
 *
 * The token expires 10 minutes after [generation][generate].
 *
 * @param value the string value of the JWT
 */
data class GitHubJwt(val value: String) {

    companion object {

        /**
         * Generates a GitHub JWT, signing it using a `.pem` file by the specified path.
         *
         * The JWT created by this minutes expires in 10 minutes. After expiration, no longer
         * can authorize [GitHubApiRequest]s.
         */
        fun generate(privateKeyPath: Path, gitHubAppId: String): GitHubJwt {
            Security.addProvider(BouncyCastleProvider())
            val pemParser = PEMParser(FileReader(privateKeyPath.toFile()))
            val converter = JcaPEMKeyConverter().setProvider("BC")

            val obj: Any = pemParser.readObject()
            val kp: KeyPair = converter.getKeyPair(obj as PEMKeyPair)
            val privateKey: PrivateKey = kp.getPrivate()

            val now = Instant.now()
            val jwt = JWT.create()
                    .withIssuer(gitHubAppId)
                    .withIssuedAt(Date.from(now))
                    .withExpiresAt(Date.from(now.plus(10, MINUTES)))
                    .sign(Algorithm.RSA256(null, privateKey as RSAPrivateKey))
            return GitHubJwt(jwt)
        }
    }
}

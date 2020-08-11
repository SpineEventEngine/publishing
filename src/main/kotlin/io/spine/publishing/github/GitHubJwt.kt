package io.spine.publishing.github

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.openssl.PEMKeyPair
import org.bouncycastle.openssl.PEMParser
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter
import java.io.FileReader
import java.nio.file.Path
import java.nio.file.Paths
import java.security.KeyPair
import java.security.PrivateKey
import java.security.Security
import java.security.interfaces.RSAPrivateKey
import java.time.Instant
import java.time.temporal.ChronoUnit
import java.util.*

data class GitHubJwt(val value: String) {

    companion object {

        fun obtain(privateKeyPath: Path, gitHubAppId: String): GitHubJwt {
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
                    .withExpiresAt(Date.from(now.plus(1, ChronoUnit.MINUTES)))
                    .sign(Algorithm.RSA256(null, privateKey as RSAPrivateKey))
            return GitHubJwt(jwt)
        }
    }
}

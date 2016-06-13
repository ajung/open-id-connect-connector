/**
 * Copyright 2016 Moritz Möller, AOE GmbH
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.mule.modules.openidconnect.automation.unit.util

import java.security.KeyPair
import java.security.KeyPairGenerator

/**
 * Util to generate random RSA key pairs for tests
 *
 * @author Moritz Möller, AOE GmbH
 *
 */
class RSAKeyGenerator {
    static KeyPair keyPairGenerator() {
        def keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(1024);
        keyGen.genKeyPair();
    }
}

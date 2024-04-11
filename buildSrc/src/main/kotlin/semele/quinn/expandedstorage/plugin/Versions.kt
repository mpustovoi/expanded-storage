package semele.quinn.expandedstorage.plugin

import org.gradle.api.JavaVersion

object Versions {
    const val EXPANDEDSTORAGE = "12.1.0-beta.1"

    // Generic
    val java = JavaVersion.VERSION_21
    const val MINECRAFT = "24w14a"
    const val PARCHMENT = "1.20.4:2024.02.25"
    const val JETBRAINS_ANNOTATIONS_VERSION = "24.1.0"
    val SUPPORTED_GAME_VERSIONS: List<String> = listOf(MINECRAFT)

    // Fabric
    const val FABRIC_LOADER = "0.15.9"
    const val FABRIC_API = "0.96.14+1.20.5"

    // NeoForge
    const val NEOFORGE = "20.5.0-alpha.24w14a.20240408.221956"

    // Quilt
    const val QUILT_LOADER = "0.23.1"
    const val QUILT_FABRIC_API = "9.0.0-alpha.6+0.96.11-1.20.4"

    // Dependencies
    const val EMI = "1.1.4+1.20.4" // https://modrinth.com/mod/emi/

    const val REI = "14.0.688" // https://modrinth.com/mod/rei/

    const val JEI = "17.3.0.49" // https://modrinth.com/mod/jei/
    const val JEI_MINECRAFT = "1.20.4"

    const val IPN = "1.10.10" // https://modrinth.com/mod/inventory-profiles-next/
    const val IPN_MINECRAFT_FABRIC = "1.20.2"

    const val LIB_IPN = "4.0.2" // https://modrinth.com/mod/libipn/
    const val LIB_IPN_MINECRAFT = "1.20.2"

    const val FABRIC_KOTLIN = "1.10.19+kotlin.1.9.23" // https://modrinth.com/mod/fabric-language-kotlin/

    const val INVENTORY_TABS = "1.1.8+1.20" // https://modrinth.com/mod/inventory-tabs/

    const val MOD_MENU = "9.0.0" // https://modrinth.com/mod/modmenu/

    const val HTM = "1.1.11" // https://modrinth.com/mod/htm/

    const val AMECS = "1.3.11+mc.1.20.4" // https://maven.siphalor.de/de/siphalor/amecs-1.20/
    const val AMECS_API = "1.5.6+mc1.20.2" // https://maven.siphalor.de/de/siphalor/amecsapi-1.20/
    const val AMECS_MINECRAFT = "1.20"

    const val CARRIER = "1.12.0" // https://modrinth.com/mod/carrier/
    const val CARDINAL_COMPONENTS = "5.4.0" // https://modrinth.com/mod/cardinal-components-api/
    const val ARRP = "0.8.1" // https://modrinth.com/mod/arrp/

    const val CARRY_ON_FABRIC = "EvDx8gEe" // https://modrinth.com/mod/carry-on/
    const val CARRY_ON_FORGE = "8a6KfB5j" // https://modrinth.com/mod/carry-on/

    const val ZETA = "1.0-14.69" // https://modrinth.com/mod/zeta/
    const val QUARK = "4.0-437.3290" // https://modrinth.com/mod/quark/
}

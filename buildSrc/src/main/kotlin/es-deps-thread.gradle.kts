import dev.compasses.expandedstorage.multiloader
import dev.compasses.expandedstorage.ModVersions
import dev.compasses.multiloader.extension.MultiLoaderExtension
import org.gradle.api.artifacts.dsl.DependencyHandler

plugins {
    id("org.quiltmc.loom")
    id("multiloader-loader")
    id("es-deps-common")
}

multiloader {
    mods {
        create("inventory-tabs") {
            curseforgeName.unsetConvention()

            requiresRepo("Sleeping Town Maven", "https://repo.sleeping.town/", setOf(
                "folk.sisby"
            ))

            artifacts { enabled ->
                ifEnabled(enabled, "folk.sisby:inventory-tabs:${ModVersions.INVENTORY_TABS}")
            }
        }

        create("rei") {
            curseforgeName = "roughly-enough-items"

            requiresRepo("Shedaniel's Maven", "https://maven.shedaniel.me/", setOf(
                "me.shedaniel",
                "me.shedaniel.cloth",
                "dev.architectury"
            ))

            artifacts { enabled ->
                ifEnabled(enabled, "me.shedaniel:RoughlyEnoughItems-fabric:${ModVersions.REI}")
            }
        }

        create("modmenu") {
            requiresRepo("TerraformersMC Maven", "https://maven.terraformersmc.com/", setOf(
                "com.terraformersmc"
            ))

            artifacts { enabled ->
                ifEnabled(enabled, "com.terraformersmc:modmenu:${ModVersions.MOD_MENU}")
            }
        }

        named("jei") {
            requiresRepo("Jared's Maven", "https://maven.blamejared.com/", setOf(
                "mezz.jei"
            ))

            artifacts { enabled ->
                modCompileOnly("mezz.jei:jei-${ModVersions.JEI_GAME}-fabric-api:${ModVersions.JEI_MOD}")
                if (enabled) {
                    modRuntimeOnly("mezz.jei:jei-${ModVersions.JEI_GAME}-fabric:${ModVersions.JEI_MOD}")
                }
            }
        }

        create("inventory-profiles-next") {
            artifacts { enabled ->
                ifEnabled(enabled, "maven.modrinth:inventory-profiles-next:fabric-${ModVersions.IPN_GAME}-${ModVersions.IPN_MOD}")
                ifEnabled(enabled, "maven.modrinth:libipn:fabric-${ModVersions.LIB_IPN_GAME_FABRIC}-${ModVersions.LIB_IPN_MOD}")
            }
        }

        create("htm") {
            artifacts {  enabled ->
                ifEnabled(enabled, "maven.modrinth:htm:${ModVersions.HTM}")
            }
        }

        named("emi") {
            requiresRepo("TerraformersMC's Maven", "https://maven.terraformersmc.com/", setOf(
                "dev.emi"
            ))

            artifacts { enabled ->
                modCompileOnly("dev.emi:emi-fabric:${ModVersions.EMI}:api")
                if (enabled) {
                    modRuntimeOnly("dev.emi:emi-fabric:${ModVersions.EMI}")
                }
            }
        }

        named("carry-on") {
            artifacts { enabled ->
                ifEnabled(enabled, "maven.modrinth:carry-on:${ModVersions.CARRY_ON_FABRIC}")
            }
        }

        create("amecs") {
            requiresRepo("Siphalor's Maven", "https://maven.siphalor.de/", setOf(
                "de.siphalor"
            ))

            artifacts { enabled ->
                modCompileOnly("de.siphalor:amecsapi-${ModVersions.AMECS_GAME}:${ModVersions.AMECS_API_MOD}")
                if (enabled) {
                    modRuntimeOnly("de.siphalor:amecs-${ModVersions.AMECS_GAME}:${ModVersions.AMECS_MOD}")
                }
            }
        }

        create("carrier") {
            disabled()

            requiresRepo("Ladysnake's Maven", "https://maven.ladysnake.org/releases/", setOf(
                "org.ladysnake.cardinal-components-api"
            ))

            requiresRepo("Ueaj's Maven", "https://ueaj.dev/maven/", setOf(
                "net.devtech"
            ))

            artifacts { enabled ->
                ifEnabled(enabled, "maven.modrinth:carrier:${ModVersions.CARRIER}")
                ifEnabled(enabled, "org.ladysnake.cardinal-components-api:cardinal-components-base:${ModVersions.CARDINAL_COMPONENTS}")
                ifEnabled(enabled, "org.ladysnake.cardinal-components-api:cardinal-components-entity:${ModVersions.CARDINAL_COMPONENTS}")
                ifEnabled(enabled, "net.devtech:arrp:${ModVersions.ARRP}")
            }
        }
    }
}

fun DependencyHandler.ifEnabled(enabled: Boolean, dependencyNotation: String) {
    if (enabled) {
        modImplementation(dependencyNotation)
    } else {
        modCompileOnly(dependencyNotation)
    }
}

package me.fzzyhmstrs.fzzy_config.validated_field_v2.number

import me.fzzyhmstrs.fzzy_config.config.ValidationResult
import net.minecraft.client.gui.widget.Widget
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlLiteral
import net.peanuuutz.tomlkt.asTomlLiteral
import net.peanuuutz.tomlkt.toInt

class ValidatedDouble(defaultValue: Double, maxValue: Double, minValue: Double): ValidatedNumber<Double>(defaultValue, minValue, maxValue) {

    override fun deserializeHeldValue(toml: TomlElement, fieldName: String): ValidationResult<Double> {
        return try{
            ValidationResult.success(toml.asTomlLiteral().toDouble())
        } catch (e: Exception){
            ValidationResult.error(defaultValue,"Problem deserializing ValidatedDouble [$fieldName]: ${e.localizedMessage}")
        }
    }

    override fun serializeHeldValue(): TomlElement {
        return TomlLiteral(storedValue)
    }

    override fun createWidget(): Widget {
        TODO("Not yet implemented")
    }


}
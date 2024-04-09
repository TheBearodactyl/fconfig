package me.fzzyhmstrs.fzzy_config.validation.minecraft

import com.mojang.serialization.JsonOps
import me.fzzyhmstrs.fzzy_config.entry.EntryValidator
import me.fzzyhmstrs.fzzy_config.fcId
import me.fzzyhmstrs.fzzy_config.screen.widget.DecorationWrappedWidget
import me.fzzyhmstrs.fzzy_config.screen.widget.OnClickTextFieldWidget
import me.fzzyhmstrs.fzzy_config.screen.widget.PopupWidget
import me.fzzyhmstrs.fzzy_config.screen.widget.PopupWidget.Builder.Position
import me.fzzyhmstrs.fzzy_config.screen.widget.SuggestionBackedTextFieldWidget
import me.fzzyhmstrs.fzzy_config.util.TomlOps
import me.fzzyhmstrs.fzzy_config.util.ValidationResult
import me.fzzyhmstrs.fzzy_config.util.ValidationResult.Companion.error
import me.fzzyhmstrs.fzzy_config.util.ValidationResult.Companion.wrap
import me.fzzyhmstrs.fzzy_config.validation.ValidatedField
import me.fzzyhmstrs.fzzy_config.validation.misc.ChoiceValidator
import me.fzzyhmstrs.fzzy_config.validation.misc.ValidatedIdentifier
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.gui.widget.ClickableWidget
import net.minecraft.registry.tag.TagKey
import net.minecraft.util.Identifier
import net.peanuuutz.tomlkt.TomlElement
import net.peanuuutz.tomlkt.TomlNull
import org.jetbrains.annotations.ApiStatus.Internal
import java.util.function.Consumer
import java.util.function.Predicate
import kotlin.jvm.optionals.getOrNull

/**
 * A validated TagKey
 *
 * By default, validation will allow any TagKey currently known by the default tags registry.
 * @param T the TagKey type
 * @param defaultValue [TagKey] - the default tag
 * @param predicate [Predicate]<[Identifier]>, Optional - use to restrict the allowable tag selection
 * @sample me.fzzyhmstrs.fzzy_config.examples.MinecraftExamples.validatedTag
 * @sample me.fzzyhmstrs.fzzy_config.examples.MinecraftExamples.validatedTagPredicated
 * @author fzzyhmstrs
 * @since 0.2.0
 */
class ValidatedTagKey<T: Any> @JvmOverloads constructor(defaultValue: TagKey<T>, private val predicate: Predicate<Identifier>? = null): ValidatedField<TagKey<T>>(defaultValue) {

    private val validator = if(predicate == null) ValidatedIdentifier.ofRegistryTags(defaultValue.registry) else ValidatedIdentifier.ofRegistryTags(defaultValue.registry, predicate)
    private val codec = TagKey.codec(defaultValue.registry)

    override fun set(input: TagKey<T>) {
        validator.validateAndSet(input.id)
        super.set(input)
    }
    @Internal
    override fun deserialize(toml: TomlElement, fieldName: String): ValidationResult<TagKey<T>> {
        return try{
            val json = TomlOps.INSTANCE.convertTo(JsonOps.INSTANCE,toml)
            val dataResult = codec.parse(JsonOps.INSTANCE,json)
            if (dataResult.isSuccess){
                ValidationResult.success(dataResult.orThrow)
            } else {
                ValidationResult.error(storedValue,"Error deserializing Validated Tag [$fieldName]: ${dataResult.error().getOrNull()?.message()}")
            }
        } catch (e: Exception){
            ValidationResult.error(storedValue,"Critical error encountered while deserializing Validated Tag [$fieldName]: ${e.localizedMessage}")
        }
    }
    @Internal
    override fun serialize(input: TagKey<T>): ValidationResult<TomlElement> {
        val encodeResult = codec.encodeStart(JsonOps.INSTANCE,input)
        if (encodeResult.isError){
            return ValidationResult.error(TomlNull,"Error serializing TagKey: ${encodeResult.error().getOrNull()?.message()}")
        }
        return try {
            ValidationResult.success(JsonOps.INSTANCE.convertTo(TomlOps.INSTANCE,encodeResult.orThrow))
        } catch (e: Exception){
            ValidationResult.error(TomlNull,"Critical Error while serializing TagKey: ${e.localizedMessage}")
        }
    }

    override fun copyStoredValue(): TagKey<T> {
        return TagKey.of(storedValue.registry,storedValue.id)
    }

    override fun instanceEntry(): ValidatedField<TagKey<T>> {
        return ValidatedTagKey(copyStoredValue(), predicate)
    }
    @Internal
    override fun isValidEntry(input: Any?): Boolean {
        return input is TagKey<*> && input.registry == storedValue.registry
    }
    @Internal
    override fun widgetEntry(choicePredicate: ChoiceValidator<TagKey<T>>): ClickableWidget {
        return DecorationWrappedWidget(OnClickTextFieldWidget({ validator.get().toString() },{ popupTagPopup(it,choicePredicate) }),"widget/decoration/tag".fcId())
    }

    @Internal
    @Environment(EnvType.CLIENT)
    private fun popupTagPopup(b: ClickableWidget, choicePredicate: ChoiceValidator<TagKey<T>>){
        val entryValidator = EntryValidator<String>{s,_ -> Identifier.tryParse(s)?.let { validator.validateEntry(it,EntryValidator.ValidationType.STRONG)}?.wrap(s) ?: error(s,"invalid Identifier")}
        val entryApplier = Consumer<String> { e -> setAndUpdate(TagKey.of(defaultValue.registry,Identifier(e))) }
        val suggestionProvider = SuggestionBackedTextFieldWidget.SuggestionProvider {s,c,cv -> validator.allowableIds.getSuggestions(s,c,cv.convert({ Identifier(it) },{ Identifier(it) }))}
        val textField = SuggestionBackedTextFieldWidget(170,20, { validator.get().toString() },choicePredicate.convert({it.id.toString()}, {it.id.toString()}),entryValidator,entryApplier,suggestionProvider)
        val popup = PopupWidget.Builder(translation())
            .addElement("text_field",textField, Position.BELOW,Position.ALIGN_LEFT)
            .addDoneButton({ textField.pushChanges(); PopupWidget.pop() })
            .positionX { _, _ -> b.x - 8 }
            .positionY { _, h -> b.y + 28 + 24 - h }
            .build()
        PopupWidget.push(popup)
        PopupWidget.focusElement(textField)
    }
}
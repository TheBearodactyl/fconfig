package me.fzzyhmstrs.fzzy_config.screen.widget.internal

import me.fzzyhmstrs.fzzy_config.fcId
import me.fzzyhmstrs.fzzy_config.screen.entry.Decorated
import me.fzzyhmstrs.fzzy_config.util.FcText
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.font.TextRenderer
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.PressableWidget
import net.minecraft.text.MutableText
import net.minecraft.util.Identifier

@Environment(EnvType.CLIENT)
internal class NoPermsButtonWidget : PressableWidget(0,0,110,20, FcText.empty()), Decorated {

    init{
        this.active = false
    }

    private val title = FcText.translatable("fc.button.noPerms")

    override fun getNarrationMessage(): MutableText {
        return this.message.copy()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        builder.put(NarrationPart.TITLE, this.narrationMessage)
        //builder.put(NarrationPart.USAGE, FcText.translatable("narration.component_list.usage"))
    }

    override fun drawScrollableText(context: DrawContext?, textRenderer: TextRenderer?, xMargin: Int, color: Int) {
        val i = x + xMargin
        val j = x + getWidth() - xMargin
        drawScrollableText(context, textRenderer, title, i, y, j, y + getHeight(), color)
    }

    override fun onPress() {
    }

    override fun decorationId(): Identifier {
        return "widget/decoration/locked".fcId()
    }
}
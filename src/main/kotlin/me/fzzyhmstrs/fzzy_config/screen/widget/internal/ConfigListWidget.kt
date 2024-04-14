package me.fzzyhmstrs.fzzy_config.screen.widget.internal

import me.fzzyhmstrs.fzzy_config.screen.LastSelectable
import me.fzzyhmstrs.fzzy_config.screen.entry.BaseConfigEntry
import me.fzzyhmstrs.fzzy_config.screen.entry.SettingConfigEntry
import me.fzzyhmstrs.fzzy_config.screen.internal.ConfigScreen
import net.fabricmc.api.EnvType
import net.fabricmc.api.Environment
import net.minecraft.client.MinecraftClient
import net.minecraft.client.gui.DrawContext
import net.minecraft.client.gui.Element
import net.minecraft.client.gui.screen.narration.NarrationMessageBuilder
import net.minecraft.client.gui.screen.narration.NarrationPart
import net.minecraft.client.gui.widget.ElementListWidget
import net.minecraft.client.search.SuffixArray
import java.util.*

@Environment(EnvType.CLIENT)
internal class ConfigListWidget(minecraftClient: MinecraftClient, width: Int, contentHeight: Int, headerHeight: Int, private val drawBackground: Boolean) :
    ElementListWidget<BaseConfigEntry>(minecraftClient, width, contentHeight, headerHeight, 24), LastSelectable
{

    constructor(minecraftClient: MinecraftClient, parent: ConfigScreen, drawBackground: Boolean = true): this(minecraftClient,parent.width,parent.layout.contentHeight,parent.layout.headerHeight, drawBackground)

    private var visibleElements = 5

    private val wholeList: List<BaseConfigEntry> by lazy{
        this.children().toList()
    }

    private val  search: SuffixArray<Int> by lazy {
        val array = SuffixArray<Int>()
        for ((i, entry) in wholeList.withIndex()){
            array.add(i,entry.name.string.lowercase(Locale.ROOT))
        }
        array.build()
        array
    }

    override var lastSelected: Element? = null

    override fun drawHeaderAndFooterSeparators(context: DrawContext) {
        if (drawBackground)
            super.drawHeaderAndFooterSeparators(context)
    }

    override fun drawMenuListBackground(context: DrawContext) {
        if (drawBackground)
            super.drawMenuListBackground(context)
    }

    override fun pushLast() {
        lastSelected = focused
    }

    override fun popLast() {
        (lastSelected as? BaseConfigEntry)?.let { focused = it }
    }

    fun updateSearchedEntries(searchInput: String): Int {
        if (searchInput == "") {
            this.replaceEntries(wholeList.toList())
            scrollAmount = 0.0
            return wholeList.size
        }
        val results = search.findAll(searchInput.lowercase(Locale.ROOT))
        val list = wholeList.filterIndexed { index, _ -> results.contains(index) }
        this.replaceEntries(list)
        scrollAmount = 0.0
        return list.size
    }

    override fun getRowWidth(): Int {
        return 260
    }

    override fun position(width: Int, height: Int, y: Int) {
        super.position(width, height, y)
        var count = 0
        for (i in 0 until this.entryCount) {
            val entryY = getRowTop(i)
            val entryYBottom = getRowBottom(i)
            if (entryY >= this.y && entryYBottom <= this.bottom){
                count++
            }
            this.getEntry(i).positionWidget(entryY)
        }
        visibleElements = count
    }

    public override fun getScrollbarX(): Int {
        return super.getScrollbarX()
    }

    override fun isSelectButton(button: Int): Boolean {
        return button == 0 || button == 1
    }

    fun getClient(): MinecraftClient{
        return this.client
    }

    fun page(up: Boolean){
        if (up){
            scrollAmount -= (visibleElements * itemHeight)
        } else {
            scrollAmount += (visibleElements * itemHeight)
        }
    }

    fun add(entry: BaseConfigEntry){
        this.addEntry(entry)
    }

    fun copy() {
        (focused as? SettingConfigEntry) ?.copyAction?.run() ?: (hoveredEntry as? SettingConfigEntry)?.copyAction?.run()
    }

    fun paste() {
        (focused as? SettingConfigEntry)?.pasteAction?.run() ?: (hoveredEntry as? SettingConfigEntry)?.pasteAction?.run()
    }

    override fun appendClickableNarrations(builder: NarrationMessageBuilder) {
        super.appendClickableNarrations(builder)
        builder.put(NarrationPart.USAGE,"")
    }

    override fun appendNarrations(builder: NarrationMessageBuilder, entry: BaseConfigEntry) {
        if(entry == focused){
            builder.put(NarrationPart.TITLE, entry.name)
        }
        super.appendNarrations(builder, entry)
    }

}
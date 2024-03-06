package me.fzzyhmstrs.fzzy_config.updates

import me.fzzyhmstrs.fzzy_config.config.Config
import me.fzzyhmstrs.fzzy_config.impl.ConfigApiImpl
import me.fzzyhmstrs.fzzy_config.util.Update
import net.minecraft.text.Text
import java.util.*

object UpdateManager{

    // Important Base Concept: SCOPE
    // basically a string mapping of the "location" of an element in a config layout, not disimilar to a file path
    //
    // Top level
    //   The namespace of the mod adding configs. The namespace of config.getId()
    //   ex. 'mymod'
    //
    // Config
    //   Next level is the config name, the path of the getId()
    //   ex. 'items'
    //
    // Subsection
    //   sections add a layer to the scope. stacks.
    //   ex. 'dropRates'
    //   
    // Element
    //   finally the element terminates the scope
    //   ex. 'oceanChests'
    //
    // Built
    //   scopes are built into translation-key-like strings
    //   ex. 'mymod.items.dropRates.oceanChests'

    private val updateMap: MutableMap<String, Updater> = mutableMapOf()
    private val changeHistory: MutableMap<String, ArrayListMultimap<Long, Text>> = mutableMapOf()
    private var currentScope = ""
    
    fun setScope(scope: String){
        currentScope = scope
    }

    fun flush(){
        updateMap.clear()
        val formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
        FC.LOGGER.info("Completed config updates:")
        FC.LOGGER.info("∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨∨")
        for ((scope, updateLog) in changeHistory){
            for ((time, updates) in updateLog){
                for (update in updates) {
                    FC.LOGGER.info("Updated scope [$scope] at [${formatter.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time),ZoneId.systemDefault()))}]: [${update.toString()}]")
                }
            }
        }
        FC.LOGGER.info("∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧∧")
        changeHistory.clear()
        
    }

    fun needsUpdatePop(updatable: Updatable): Boolean {
        val key = updatable.getUpdateKey()
        val updater = updateMap[key] ?: return false
        if (!updater.canUndo()) return false
        return updatable.popState()
    }

    fun needsUpdatePeek(updatable: Updatable): Boolean {
        val updater = updateMap[key] ?: return false
        if (!updater.canUndo()) return false
        return updatable.peekState()
    }

    fun update(key: String, update: Update) {
        updateMap[key]?.update(update)
        changeCount++
    }

    fun getChangeCount(scope: String): Int {
        var count = 0
        for (update in getScopedUpdates(scope)){
            count += update.changeCount()
        }
        return count
    }

    fun updateCurrentChangeCount(): Int {
        return getChangeCount(currentScope)
    }

    fun revert(scope: String) {
        for (update in getScopedUpdates(scope)){
            update.revert()
        }        
    }

    fun revertCurrent() {
        revert(currentScope)
    }

    fun addUpdateMessage(key: String,text: Text) {
        changeHistory.computeIfAbsent(key){ArrayListMultimap.create()}.put(System.currentTimeMillis(),text)
    }

    fun getScopedUpdates(scope: String): Collection<Updater> {
        return (if(scope.isEmpty()) updateMap.keys else updateMap.keys.filter{ it.startsWith(scope) }).mapNotNull { updateMap[it] }
    }

    fun<T: Config> applyKeys(config: T) {
        ConfigApiImpl.walk(config,config.getId().toShortTranslationKey(),true) {str, v -> if (v is Updatable) v.setUpdateKey(str)}
    }

    fun<T: Config> getSyncUpdates(config: T): Map<String, FzzySerializable> {
        val map: MutableMap<String, FzzySerializable> = mutableMapOf()
        ConfigApiImpl.walk(config,config.getId().toShortTranslationKey(),false) {str, v -> if (v is Updatable && v is FzzySerializable) { if (needsUpdatePop(v)) map[str] = v }}
        return map
    }

}

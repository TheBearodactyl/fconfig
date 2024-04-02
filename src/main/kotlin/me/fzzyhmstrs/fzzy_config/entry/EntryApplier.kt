package me.fzzyhmstrs.fzzy_config.entry

import java.util.function.Consumer

/**
 * Applies input values into a complex [Entry]
 *
 * SAM: [applyEntry] consumes an instance of T
 * @param T the type of the Entry stored value
 * @author fzzyhmstrs
 * @since 0.2.0
 */
@FunctionalInterface
fun interface EntryApplier<T>: Consumer<T> {
    fun applyEntry(input: T){
        accept(input)
    }

}
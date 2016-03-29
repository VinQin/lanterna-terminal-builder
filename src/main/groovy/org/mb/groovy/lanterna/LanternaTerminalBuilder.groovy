package org.mb.groovy.lanterna

import java.awt.*

class LanternaTerminalBuilder {
    LanternaTerminal terminal(Map attr, @DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = LanternaTerminal) Closure cl) {
        LanternaTerminal terminal = new LanternaTerminal()

        if (terminal instanceof Frame) {
            Frame frame = (Frame) terminal

            if (attr?.title)
                frame.title = attr.title
        }

        Closure code = cl.rehydrate(terminal, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()
        return terminal
    }

    LanternaTerminal terminal(@DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = LanternaTerminal) Closure cl) {
        return terminal(null, cl)
    }
}

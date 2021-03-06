package org.mb.groovy.lanterna

import com.googlecode.lanterna.gui2.BasicWindow
import com.googlecode.lanterna.gui2.Window

class WindowBuilder {
    final private LanternaWindow window = new LanternaWindow()

    WindowBuilder(LanternaTerminal terminal, Map attr) {
        window.underlying = new BasicWindow()
        window.terminal = terminal

        Collection<Window.Hint> hints = []

        if (attr?.title)
            window.underlying.title = attr.title
        if (attr?.bareTerminal && attr.bareTerminal as boolean) {
            hints << Window.Hint.FULL_SCREEN
            // Main window has no border, but dialogs still do!
            hints << Window.Hint.NO_DECORATIONS
        }
        window.underlying.hints = hints
    }

    LanternaWindow getWindow() {
        return window
    }

    void panel(@DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = PanelBuilder) Closure cl) {
        panel(null, cl)
    }

    void panel(Map attr, @DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = PanelBuilder) Closure cl) {
        if (window.underlying.component)
            throw new LanternaBuilderException('underlying can only contain a single panel')

        PanelBuilder builder = new PanelBuilder(window, attr)
        Closure code = cl.rehydrate(builder, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        window.underlying.setComponent(builder.component)
    }
}

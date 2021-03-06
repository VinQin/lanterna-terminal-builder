package org.mb.groovy.lanterna

import com.googlecode.lanterna.TerminalSize
import com.googlecode.lanterna.TextColor
import com.googlecode.lanterna.graphics.PropertiesTheme
import com.googlecode.lanterna.graphics.Theme
import com.googlecode.lanterna.gui2.MultiWindowTextGUI
import com.googlecode.lanterna.gui2.TextGUI
import com.googlecode.lanterna.gui2.dialogs.*
import com.googlecode.lanterna.input.KeyStroke
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.TerminalScreen
import com.googlecode.lanterna.terminal.DefaultTerminalFactory
import com.googlecode.lanterna.terminal.Terminal

import javax.swing.JFrame
import javax.swing.WindowConstants
import java.awt.Frame
import java.util.regex.Pattern

class LanternaTerminal {
    private Screen screen
    private MultiWindowTextGUI gui
    private Map<String, LanternaWindow> windows = [:]
    private TextGUI.Listener keyListener
    private boolean isBareTerminal = false

    LanternaTerminal(Map attr) {
        // Setup terminal and screen layers
        Terminal terminal = new DefaultTerminalFactory().createTerminal();

        if (terminal instanceof Frame) {
            Frame frame = terminal as Frame

            if (attr?.title)
                frame.title = attr.title
        }

        if (terminal instanceof JFrame)
            (terminal as JFrame).defaultCloseOperation = WindowConstants.DISPOSE_ON_CLOSE

        screen = new TerminalScreen(terminal);
        screen.startScreen();

        if (attr?.bareTerminal instanceof Boolean && attr.bareTerminal as boolean) {
            gui = new MultiWindowTextGUI(screen, TextColor.ANSI.DEFAULT)
            gui.theme = loadMinimalTheme()
            isBareTerminal = true
        } else {
            gui = new MultiWindowTextGUI(screen);
        }
    }

    Theme loadMinimalTheme() {
        Properties p = new Properties()
        InputStream is = this.class.classLoader.getResourceAsStream('minimal-theme.properties')
        p.load(is)
        Theme theme = new PropertiesTheme(p)
        return theme
    }

    void window(Map attr, @DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = WindowBuilder) Closure cl) {
        if (!attr.id)
            throw new LanternaBuilderException("'id' attribute is mandatory for windows")

        if (isBareTerminal)
            attr.bareTerminal = true

        WindowBuilder builder = new WindowBuilder(this, attr)

        Closure code = cl.rehydrate(builder, this, this)
        code.resolveStrategy = Closure.DELEGATE_ONLY
        code()

        windows[attr.id as String] = builder.window

        gui.addWindow(builder.window.underlying);
    }

    void onKey(Closure cl) {
        keyListener = new TextGUI.Listener() {
            @Override
            boolean onUnhandledKeyStroke(TextGUI textGUI, KeyStroke keyStroke) {
                cl(keyStroke)
            }
        }
        gui.addListener(keyListener)
    }

    Map<String, LanternaWindow> getWindows() {
        return windows
    }

    void waitFor(String id) {
        windows[id]?.underlying?.waitUntilClosed()
    }

    private void enterModal() {
        if (keyListener)
            gui.removeListener(keyListener)
    }

    private void leaveModal() {
        if (keyListener)
            gui.addListener(keyListener)
    }

    String messageDialog(Map attr, String text) {
        enterModal()
        try {
            MessageDialogBuilder dialogBuilder = new MessageDialogBuilder()
            dialogBuilder.text = text

            if (attr?.title)
                dialogBuilder.title = attr.title as String
            if (attr?.button) {
                // will throw an IllegalArgumentException if the user of the DSL
                // specified an invalid button name!
                dialogBuilder.addButton(MessageDialogButton.valueOf(attr.button as String))
            }
            if (attr?.buttons instanceof Collection) {
                def buttons = attr.buttons as Collection
                if (buttons.every { it instanceof String }) {
                    buttons.each {
                        dialogBuilder.addButton(MessageDialogButton.valueOf(it as String))
                    }
                }
            }

            return dialogBuilder.build().showDialog(gui).toString()
        } finally {
            leaveModal()
        }
    }

    String textInputDialog(Map attr) {
        textInputDialog(attr, '')
    }

    String textInputDialog(Map attr, String initialContent) {
        enterModal()
        try {
            TextInputDialogBuilder dialogBuilder = new TextInputDialogBuilder()

            TerminalSize size = AbstractBuilder.getSize(attr)
            dialogBuilder.setInitialContent(initialContent)
            if (size)
                dialogBuilder.setTextBoxSize(size)
            if (attr?.title)
                dialogBuilder.setTitle(attr.title as String)
            if (attr?.description)
                dialogBuilder.setDescription(attr.description as String)
            if (attr?.password)
                dialogBuilder.setPasswordInput(true)
            if (attr?.validationPattern) {
                // also need an errorMessage then!
                if (!attr.errorMessage)
                    throw new LanternaBuilderException('errorMessage is required when using validationPattern')

                def pattern = Pattern.compile(attr.validationPattern as String)
                dialogBuilder.setValidationPattern(pattern, attr.errorMessage as String)
            }

            return dialogBuilder.build().showDialog(gui)
        } finally {
            leaveModal()
        }
    }

    File fileDialog(File file) {
        fileDialog(null, file)
    }

    File fileDialog(Map attr, File file) {
        enterModal()
        try {
            FileDialogBuilder builder = new FileDialogBuilder()

            TerminalSize size = AbstractBuilder.getSize(attr)
            if (size)
                builder.suggestedSize = size
            if (attr?.title)
                builder.setTitle(attr.title as String)
            if (attr?.description)
                builder.setDescription(attr.description as String)
            if (attr?.actionLabel)
                builder.actionLabel = attr.actionLabel as String
            if (attr?.showHidden)
                builder.showHiddenDirectories = attr.showHidden as boolean

            builder.selectedFile = file

            return builder.build().showDialog(gui)
        } finally {
            leaveModal()
        }
    }

    void actionListDialog(@DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = ActionListDialogBuilderImpl) Closure cl) {
        actionListDialog(null, cl)
    }

    void actionListDialog(Map attr, @DelegatesTo(strategy = Closure.DELEGATE_ONLY, value = ActionListDialogBuilderImpl) Closure cl) {
        enterModal()
        try {
            ActionListDialogBuilder dialogBuilder = new ActionListDialogBuilder()

            TerminalSize size = AbstractBuilder.getSize(attr)
            if (size)
                dialogBuilder.listBoxSize = size
            if (attr?.title)
                dialogBuilder.setTitle(attr.title as String)
            if (attr?.description)
                dialogBuilder.setDescription(attr.description as String)

            ActionListDialogBuilderImpl builder = new ActionListDialogBuilderImpl(dialogBuilder)
            Closure code = cl.rehydrate(builder, this, this)
            code.resolveStrategy = Closure.DELEGATE_ONLY
            code()

            dialogBuilder.build().showDialog(gui)
        } finally {
            leaveModal()
        }
    }

    Object listSelectDialog(Collection coll) {
        listSelectDialog(null, coll)
    }

    Object listSelectDialog(Map attr, Collection coll) {
        enterModal()
        try {
            ListSelectDialogBuilder builder = new ListSelectDialogBuilder()
            TerminalSize size = AbstractBuilder.getSize(attr)

            if (size)
                builder.listBoxSize = size

            // Check for /presence/ of attribute, not for truth!
            if (attr?.canCancel != null)
                builder.canCancel = attr.canCancel

            coll.each {
                builder.addListItem(it)
            }

            builder.build().showDialog(gui)
        } finally {
            leaveModal()
        }
    }
}

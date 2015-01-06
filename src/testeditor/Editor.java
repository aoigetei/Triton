package testeditor;

import java.awt.*;
import java.awt.event.*;
import static java.awt.image.ImageObserver.WIDTH;
import javax.swing.*;
import javax.swing.event.*;
import java.io.*;
import static javax.swing.Action.MNEMONIC_KEY;
import javax.swing.undo.CannotUndoException;
import javax.swing.undo.UndoManager;
import org.fife.ui.rtextarea.*;
import org.fife.ui.rsyntaxtextarea.*;

public class Editor extends JFrame {
    //Filename
    File filename = new File("Untitled");
    
    int count = 0;
    String ext; //file save extension
    String[] openFileList = new String[20];
    RTextScrollPane[] panes = new RTextScrollPane[10];

    //... Components 
    RSyntaxTextArea    editArea;
    RSyntaxTextArea[]    tabArea;
    
    JPanel panelarea;
    JPanel content = new JPanel();
    JFileChooser fileChooser = new JFileChooser();
    UndoManager manager = new UndoManager();
    DefaultListModel listModel = new DefaultListModel();
    JList openFiles = new JList(listModel);
    JTabbedPane tabs = new JTabbedPane();
    
    
    //... Create actions for menu items, buttons, ...
    Action openAction = new OpenAction();
    Action saveAction = new SaveAction();
    Action undoAction = new UndoAction();
    Action redoAction = new RedoAction();
    Action exitAction = new ExitAction(); 
    Action saveAsAction = new SaveAsAction();
    
    public Editor() {
//... Create scrollable text area.
// This is the primary coding area 
// and this uses the RsyntaxTextArea for syntax
// highlighting and creates line numbering
        
        
        textArea();
        
        RTextScrollPane scrollingText = new RTextScrollPane(editArea);
        scrollingText.setFoldIndicatorEnabled(true);       
        
        tabs.addTab(filename.getName(), scrollingText);
        tabs.setTabPlacement(2);
        tabs.setBackground(Color.gray);
        tabs.addChangeListener(changeListener);
       
//        tabs.addMouseListener(new MouseAdapter(){
//            @Override
//            public void mouseClicked(MouseEvent e){
//                if(e.getButton() == MouseEvent.BUTTON3){
//                    JPopupMenu pop = new JPopupMenu();
//                    pop.add(openAction);
//                }
//            }
//        });
        
        
//-- Create a content pane, set layout, add component.
        
        content.setLayout(new BorderLayout());
        content.setBackground(Color.white);
        content.add(tabs, BorderLayout.CENTER);
        
//... Create menubar
        JMenuBar menuBar = new JMenuBar();
        
// Create menu item
        JMenu fileMenu = menuBar.add(new JMenu("File"));
        fileMenu.add(openAction);       // Note use of actions, not text.
        fileMenu.add(saveAction);
        fileMenu.add(saveAsAction);
        fileMenu.addSeparator(); 
        fileMenu.add(exitAction);
        
        JMenu fileEdit = menuBar.add(new JMenu("Edit"));
        fileEdit.add(undoAction);       // Note use of actions, not text.
        fileEdit.add(redoAction);
        
//... Set window content and menu.
        setContentPane(content);
        setJMenuBar(menuBar);
        
//Language Selector
        String[] langs = {"Java","C++","C","HTML","Python", "None"};
        
        final JComboBox selector = new JComboBox(langs);
        MyItemListener listener = new MyItemListener();
        selector.addItemListener(listener);
        
//Add a toolbar
        JToolBar toolbar = new JToolBar();
        toolbar.setRollover(true);
        toolbar.add(selector);
        toolbar.setForeground(Color.red);
        content.add(toolbar, BorderLayout.NORTH);
        
//... Set other window characteristics.
      //  this.setIconImage(Toolkit.getDefaultToolkit().getImage(getClass().getResource("triton.png")));
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setTitle("Triton");
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
   
     ChangeListener changeListener = new ChangeListener() {
      public void stateChanged(ChangeEvent changeEvent) {
        JTabbedPane sourceTabbedPane = (JTabbedPane) changeEvent.getSource();
        int index = sourceTabbedPane.getSelectedIndex();
      }
    };
     
    
    class MyItemListener implements ItemListener{
        @Override
        public void itemStateChanged(ItemEvent e){
            if(e.getStateChange() == ItemEvent.SELECTED){
                Object item = e.getItem();
                chooseSyntax((String)item);
            }
        }
    }
   
// Choose texthighlighting scheme
//could also possibly be used for setting save extension
    public void chooseSyntax(String val){
        switch(val){
            case "C": 
                editArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_C);
                ext = ".c";
                break;
            case "C++": 
                editArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CPLUSPLUS);
                ext = ".cpp";
                break;
            case "Java":
                editArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVA);
                ext = ".java";
                break;
            case "HTML":
                editArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
                ext = ".html";
                break;
            case "Python":
                editArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_PYTHON);
                ext = ".py";
                break;
            default:
                editArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_NONE);
                ext = ".txt";
                break;
        }
    }
    
    class OpenAction extends AbstractAction {
        public OpenAction() {
            super("Open...");
            putValue(MNEMONIC_KEY, new Integer('O'));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            int retval = fileChooser.showOpenDialog(Editor.this);
            if (retval == JFileChooser.APPROVE_OPTION) {
                filename = fileChooser.getSelectedFile();
                try {
                    FileReader reader = new FileReader(filename);
                    //Automatically updates the JList of open files
                    panes[count] = new RTextScrollPane(textArea());
                    editArea.read(reader,"");
                    tabs.addTab(filename.getName(), panes[count]);
                    count++;
                } catch (IOException ex) {
                    System.out.println(e);
                    System.exit(1);
                }
            }
        }
    }
    
    public RSyntaxTextArea textArea(){
        editArea = new RSyntaxTextArea(15, 80);
        editArea.setFont(new Font("monospaced", Font.PLAIN, 14));
        editArea.setCodeFoldingEnabled(true);
        editArea.setAntiAliasingEnabled(true);
        chooseSyntax("Java");
        return editArea;
    }
    
    class SaveAsAction extends AbstractAction {
        SaveAsAction() {
            super("SaveAs...");
            putValue(MNEMONIC_KEY, new Integer('S'));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            //fileChooser.setCurrentDirectory(new File("C:\Users\aoige_000\Documents\Triton"));
            int retval = fileChooser.showSaveDialog(Editor.this);
            
            if (retval == JFileChooser.APPROVE_OPTION) {
                try {
                    FileWriter writer = new FileWriter(fileChooser.getSelectedFile()+ext);
                    editArea.write(writer);  // Use TextComponent write
                    tabs.setTitleAt(tabs.getSelectedIndex(), filename.getName());
                    
                   // writer.close();
                } catch (IOException ex) {
                    JOptionPane.showMessageDialog(Editor.this, ex);
                    System.exit(1);
                }
            }
        }
    }
    
    class SaveAction extends AbstractAction{
        SaveAction(){
            super("Save...");
        }
        @Override
        public void actionPerformed(ActionEvent evt){
            if(filename.getName() != "Untitled"){
                try{
                    FileWriter writer = new FileWriter(filename);
                    editArea.write(writer);
                    //writer.close();
                }catch(IOException ex){
                    JOptionPane.showMessageDialog(Editor.this, ex);
                    System.exit(1);
                }
            }
            else
            {
                saveAsAction.actionPerformed(evt);
            }
        }
    }
    
    class UndoAction extends AbstractAction {
        UndoAction() {
            super("Undo");
            //putValue(MNEMONIC_KEY, new Integer('S'));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
                try {
                    manager.undo();
                } catch (CannotUndoException ex) {
                }
        }
    }
    
    class RedoAction extends AbstractAction {
        RedoAction() {
            super("Redo");
            //putValue(MNEMONIC_KEY, new Integer('S'));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
                try {
                    manager.redo();
                } catch (CannotUndoException ex) {
                }
        }
    }
    
    class ExitAction extends AbstractAction {
        public ExitAction() {
            super("Close");
            putValue(MNEMONIC_KEY, new Integer('X'));
        }
        @Override
        public void actionPerformed(ActionEvent e) {
            System.exit(0);
        }
    }
}
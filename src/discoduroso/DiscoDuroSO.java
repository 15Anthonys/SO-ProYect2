/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package discoduroso;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

/**
 *
 * @author dugla
 */
public class DiscoDuroSO {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        // TODO code application logic here
        
        SwingUtilities.invokeLater(() -> {
            UIDisco myWindow = UIDisco.getInstance();
            myWindow.setVisible(true);
        });
        
        
    }
    
}

package library;



import java.awt.Graphics;
import java.awt.Image;
import java.awt.LayoutManager;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

public class background_panel extends JPanel {
    private Image backgroundImage;

    public background_panel(String imagePath) {
        try {
            this.backgroundImage = (new ImageIcon(imagePath)).getImage();
        } catch (Exception var3) {
            Exception e = var3;
            e.printStackTrace();
        }

        this.setLayout((LayoutManager)null);
    }

    protected void paintComponent(Graphics g){
        super.paintComponent(g);
        if (this.backgroundImage != null) {
            g.drawImage(this.backgroundImage, 0, 0, this.getWidth(), this.getHeight(), this);
        }

    }
}
package game;

import javax.swing.*;
import java.awt.*;

public class ImagePanel extends JPanel {
    private Image backgroundImage;

    // 생성자에서 이미지 파일 경로를 받아들임
    public ImagePanel(String imagePath) {
        backgroundImage = new ImageIcon(imagePath).getImage();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        if (backgroundImage != null) {
            // 패널의 크기에 맞춰 배경 이미지 그리기
            g.drawImage(backgroundImage, 0, 0, getWidth(), getHeight(), this);
        }
    }
}


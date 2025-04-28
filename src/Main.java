import javax.swing.*;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {

        JFrame frame = new JFrame("PacMan");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setLocationRelativeTo(null);
      //  frame.setSize(boardWidth,boardHeight);
        frame.setResizable(false);

        PacMan pacManGame = new PacMan();
        frame.add(pacManGame);
        frame.pack();
        frame.setVisible(true);
        pacManGame.requestFocus();


    }

}
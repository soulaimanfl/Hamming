import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

public class Hamming extends JFrame {
    private RoundButton coder = new RoundButton("Coder");
    private RoundButton corriger = new RoundButton("Corriger");
    private RoundButton quitter = new RoundButton("Quitter");
    private JLabel txt = new JLabel("Entrer le code");
    private JLabel txt1 = new JLabel("Code Hamming");
    private JTextField code = new JTextField("", 10);
    private String texte;

    public Hamming() {
        setTitle("Code Hamming");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        GroupLayout layout = new GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setAutoCreateGaps(true);
        layout.setAutoCreateContainerGaps(true);

        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(txt)
                        .addComponent(code))
                .addComponent(txt1)
                .addGroup(layout.createSequentialGroup()
                        .addComponent(coder)
                        .addComponent(corriger)
                        .addComponent(quitter))
        );

        layout.setVerticalGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(txt)
                        .addComponent(code))
                .addComponent(txt1)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                        .addComponent(coder)
                        .addComponent(corriger)
                        .addComponent(quitter))
        );

        setPreferredSize(new Dimension(600, 400));
        setLocationRelativeTo(null);

        try {
            UIManager.setLookAndFeel("javax.swing.plaf.nimbus.NimbusLookAndFeel");
            SwingUtilities.updateComponentTreeUI(this);
        } catch (Exception e) {
            e.printStackTrace();
        }

        pack();
        setVisible(true);

        quitter.addActionListener(new MyListener());
        coder.addActionListener(new MyListener());
        corriger.addActionListener(new MyListener());
    }

    public class MyListener implements ActionListener {
        public void actionPerformed(ActionEvent ae) {
            String tape = ae.getActionCommand();
            if ("Quitter".equals(tape)) {
                System.exit(0);
            } else if ("Coder".equals(tape)) {
                coderLogic();
            } else if ("Corriger".equals(tape)) {
                corrigerLogic();
            }
        }
    }

    public void coderLogic() {
        texte = code.getText();
        Graphics g = getGraphics();
        String zero = "0";
        g.setFont(new Font("SansSerif", Font.PLAIN, 16));
        paint(g);

        int n = 1;
        while ((Math.pow(2, n) - 1 - n) < texte.length())
            n++;

        if (!nonbinaire(texte, g)) {
            // Étendre le code de zéros
            if ((Math.pow(2, n) - 1 - n) != texte.length()) {
                for (int i = texte.length(); i < (Math.pow(2, n) - 1 - n); i++) {
                    texte = zero + texte;
                }
            }

            // Bits de contrôle zéros aux bons endroits
            StringBuilder mot = new StringBuilder(texte);
            for (int c = 0; c < n; c++) {
                int endroit = mot.length() - (int) Math.pow(2, c) + 1;
                mot.insert(endroit, '0');
            }

            for (int c = 0; c < n; c++) {
                int valeur = 0;
                for (Integer i : controlebits(c, n)) {
                    int endroit = (int) Math.pow(2, n) - i - 1;
                    valeur = (valeur + Character.getNumericValue(mot.charAt(endroit))) % 2;
                }
                int endroit = mot.length() - (int) Math.pow(2, c);
                mot.setCharAt(endroit, Character.forDigit(valeur, 10));
            }

            g.drawString("Resultat :", 280, 150);
            g.drawString(" " + mot, 300 - (mot.length() * 4), 200);
        }
    }


    public void corrigerLogic() {
        texte = code.getText();
        Graphics g = getGraphics();
        Font stringFont = new Font("SansSerif", Font.PLAIN, 16);
        g.setFont(stringFont);
        paint(g);

        boolean suivant = true;

        // Vérification de la taille
        int n = 1;
        while (Math.pow(2, n) - 1 < texte.length()) {
            n++;
        }

        if (Math.pow(2, n) - 1 != texte.length()) {
            showMessage("Taille invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            suivant = false;
        }

        if (suivant && !nonbinaire(texte, g)) {
            int[] control = new int[n];

            for (int c = 0; c < n; c++) {
                for (Integer i : controlebits(c, n)) {
                    int endroit = (int) Math.pow(2, n) - i - 1;
                    control[c] = (control[c] + Character.getNumericValue(texte.charAt(endroit))) % 2;
                }
            }

            int indiceRetour = 0;
            int indiceRouge = 0;

            for (int i = 0; i < n; i++) {
                indiceRetour += control[i] * Math.pow(2, i);

                if (indiceRetour != 0) {
                    indiceRouge = i;
                }
            }

            if (indiceRetour != 0) {
                showMessage("Mot invalide", "Erreur", JOptionPane.ERROR_MESSAGE);
            } else {
                showMessage("Mot valide", "Succès", JOptionPane.INFORMATION_MESSAGE);
            }

            // Interface écriture du code
            for (int i = 0; i < texte.length(); i++) {
                if (i == indiceRouge && indiceRetour != 0) {
                    g.setColor(Color.RED);
                }
                g.drawString(" " + texte.charAt(i), (300 + (10 * i) - (texte.length() * 6)), 200);

                if (i == indiceRouge && indiceRetour != 0) {
                    g.setColor(Color.BLACK);
                }
            }
        }
    }

    private void showMessage(String message, String title, int messageType) {
        JOptionPane.showMessageDialog(this, message, title, messageType);
    }

    private ArrayList<Integer> controlebits(int c, int n) {
        ArrayList<Integer> tableau = new ArrayList<>();
        for (int i = 0; i < Math.pow(2, n); i++) {
            String s = String.format("%" + n + "s", Integer.toBinaryString(i)).replace(' ', '0');
            int endroit = n - c - 1;
            if (s.charAt(endroit) == '1') {
                tableau.add(i);
            }
        }
        return tableau;
    }

    public boolean nonbinaire(String c, Graphics g) {
        for (int i = 0; i < c.length(); i++) {
            if (texte.charAt(i) != '0' && texte.charAt(i) != '1') {
                g.drawString("Mot non binaire dans position " + i + " l'element " + texte.charAt(i) + " est refus !", 100, 200);
                return true;
            }
        }
        return false;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Hamming::new);
    }
}

class RoundButton extends JButton {
    public RoundButton(String label) {
        super(label);
        Dimension size = getPreferredSize();
        size.width = size.height = Math.max(size.width, size.height);
        setPreferredSize(size);
        setContentAreaFilled(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        if (getModel().isArmed()) {
            g.setColor(Color.lightGray);
        } else {
            g.setColor(getBackground());
        }
        g.fillRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 20, 20);
        super.paintComponent(g);
    }

    @Override
    protected void paintBorder(Graphics g) {
        g.setColor(Color.darkGray);
        g.drawRoundRect(0, 0, getSize().width - 1, getSize().height - 1, 20, 20);
    }
}
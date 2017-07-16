/**
 * 
 */
package mevenk.SynchronousConsole;

import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.Window.Type;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.SwingWorker;
import javax.swing.border.EtchedBorder;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import mevenk.SynchronousConsole.util.SynchronousConsoleUtil;

/**
 * @author Venkatesh
 *
 */
public class SynchronousConsole {

	private static final String LINE_SEPARATOR = SynchronousConsoleUtil.LINE_SEPARATOR;

	private JFrame frameSynchronousConsole;

	private JButton btnDisplayDate;

	private JButton btnSelectFile;
	private JLabel lblSelectedfile;
	private JButton btnParseFile;
	private JFileChooser fileChooser;

	private File selectedFile;

	private JProgressBar progressBar;

	private JPopupMenu contextMenu;

	private static JTextPane resultTextPane = new JTextPane();

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {

			PrintStream resultTextPanePrintStream = new PrintStream(new ResultTextPaneOutputStream(resultTextPane),
					true);
			System.setOut(resultTextPanePrintStream);
			System.setErr(resultTextPanePrintStream);
			System.out.println("Welcome, " + SynchronousConsoleUtil.USER_NAME);
			System.out.println(new Date());

			SwingUtilities.invokeLater(new Runnable() {

				@Override
				public void run() {
					new SynchronousConsole().frameSynchronousConsole.setVisible(true);

				}
			});

		} catch (Exception exception) {
			JOptionPane.showMessageDialog(null, "Error!!" + LINE_SEPARATOR + exception.getMessage(), "Error",
					JOptionPane.ERROR_MESSAGE);
		}

	}

	/**
	 * 
	 */
	public SynchronousConsole() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frameSynchronousConsole = new JFrame();
		frameSynchronousConsole.setAlwaysOnTop(true);
		frameSynchronousConsole.setIconImage(Toolkit.getDefaultToolkit()
				.getImage(SynchronousConsole.class.getResource("/mevenk/image/mevenkGitHubLogo.png")));
		frameSynchronousConsole.setType(Type.UTILITY);
		frameSynchronousConsole.setTitle("Synchronous Console");
		frameSynchronousConsole.setResizable(false);

		frameSynchronousConsole.setBounds(100, 100, 600, 360);
		frameSynchronousConsole.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frameSynchronousConsole.getContentPane().setLayout(null);

		Dimension screenDimension = Toolkit.getDefaultToolkit().getScreenSize();
		frameSynchronousConsole.setLocation(screenDimension.width / 2 - frameSynchronousConsole.getSize().width / 2,
				screenDimension.height / 2 - frameSynchronousConsole.getSize().height / 2);

		JScrollPane scrollPaneResultTextPane = new JScrollPane();
		scrollPaneResultTextPane.setBounds(10, 80, 570, 245);
		frameSynchronousConsole.getContentPane().add(scrollPaneResultTextPane);

		scrollPaneResultTextPane.setViewportView(resultTextPane);
		resultTextPane.setEditable(false);
		resultTextPane.setFont(new Font("Consolas", Font.BOLD, 12));
		resultTextPane.setForeground(Color.WHITE);
		resultTextPane.setBackground(Color.BLACK);

		btnDisplayDate = new JButton("Display Date");
		btnDisplayDate.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnDisplayDate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(new Date());
			}
		});
		btnDisplayDate.setBounds(10, 11, 125, 30);
		frameSynchronousConsole.getContentPane().add(btnDisplayDate);

		btnSelectFile = new JButton("File");
		btnSelectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int fileChooserReturnVal = fileChooser.showOpenDialog(frameSynchronousConsole);
				if (fileChooserReturnVal == JFileChooser.APPROVE_OPTION) {
					selectedFile = fileChooser.getSelectedFile();
					lblSelectedfile.setText(selectedFile.getName());
					lblSelectedfile.setToolTipText(selectedFile.getPath());
				}

			}
		});
		btnSelectFile.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnSelectFile.setBounds(155, 11, 75, 30);
		frameSynchronousConsole.getContentPane().add(btnSelectFile);

		lblSelectedfile = new JLabel("");
		lblSelectedfile.setBounds(255, 11, 175, 30);
		lblSelectedfile.setToolTipText("Enter package name");
		lblSelectedfile.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectedfile.setFont(new Font("Trebuchet MS", Font.BOLD, 14));
		lblSelectedfile.setBorder(new EtchedBorder(EtchedBorder.LOWERED, null, null));
		frameSynchronousConsole.getContentPane().add(lblSelectedfile);

		btnParseFile = new JButton("Parse File");
		btnParseFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				resultTextPane.setText("");
				progressBar.setValue(0);
				progressBar.setString(null);

				System.out.println("Selected File : " + selectedFile.getPath());

				try {
					String mimeType = Files.probeContentType(
							FileSystems.getDefault().getPath(selectedFile.getParent(), selectedFile.getName()));
					System.out.println("MIME Type : " + mimeType);
					if (mimeType == null || !mimeType.contains("text")) {
						System.out.println("Text File Required !!");
						return;
					}
				} catch (Exception exception) {
					exception.printStackTrace();
				}

				new FileParseInBackground().execute();

			}
		});
		btnParseFile.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnParseFile.setBounds(455, 11, 125, 30);
		frameSynchronousConsole.getContentPane().add(btnParseFile);

		contextMenu = new JPopupMenu();

		addPopup(resultTextPane, contextMenu);

		JButton copyButton = new JButton(" Copy  ");
		copyButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {

				String resultTextPaneText = resultTextPane.getSelectedText();
				if (resultTextPaneText == null || resultTextPaneText == "") {
					resultTextPaneText = resultTextPane.getText();
				}

				if (resultTextPaneText != null && resultTextPaneText.length() > 0) {
					resultTextPaneText.replaceAll("\\r", "");
					StringSelection packageNameTextFieldTextStringSelection = new StringSelection(resultTextPaneText);
					Toolkit.getDefaultToolkit().getSystemClipboard()
							.setContents(packageNameTextFieldTextStringSelection, null);
				}

				if (contextMenu.isShowing()) {
					contextMenu.setVisible(false);
				}
			}
		});
		contextMenu.add(copyButton);

		JButton clearConsoleButton = new JButton(" Clear ");
		clearConsoleButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				resultTextPane.setText("");
				if (contextMenu.isShowing()) {
					contextMenu.setVisible(false);
				}
			}
		});
		contextMenu.add(clearConsoleButton);

		progressBar = new JProgressBar();
		progressBar.setFont(new Font("Tahoma", Font.BOLD | Font.ITALIC, 12));
		progressBar.setForeground(Color.GREEN);
		progressBar.setStringPainted(true);
		progressBar.setBounds(10, 52, 570, 17);
		frameSynchronousConsole.getContentPane().add(progressBar);

	}

	private static void addPopup(Component component, final JPopupMenu popup) {
		component.addMouseListener(new MouseAdapter() {
			public void mousePressed(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					showMenu(e);
				}
			}

			private void showMenu(MouseEvent e) {
				popup.show(e.getComponent(), e.getX(), e.getY());
			}
		});
	}

	class FileParseInBackground extends SwingWorker<Void, Void> {

		@Override
		protected Void doInBackground() {

			keepWaiting();

			try {

				int noOfLines = SynchronousConsoleUtil.lineCount(selectedFile);

				boolean percentageDisplayRequired = false;

				if (noOfLines != -1) {
					percentageDisplayRequired = true;
				} else {
					System.out.println("Line Parse Percentage Display Error !!");
				}

				int counter = 0;
				int percentComplete = 0;
				int previousPercent = 0;

				String fileLine = "";
				BufferedReader lineBufferedReader = new BufferedReader(new FileReader(selectedFile));
				StringBuffer lineStringBuffer = new StringBuffer(fileLine);

				while ((fileLine = lineBufferedReader.readLine()) != null) {
					lineStringBuffer.append(fileLine + LINE_SEPARATOR);

					percentComplete = (int) Math.floor((++counter) * 100 / noOfLines);

					// Percentage Display

					if (percentComplete != previousPercent) {
						progressBar.setValue((int) percentComplete);
						if (percentageDisplayRequired) {
							printOnSameLine("Please Wait [" + percentComplete + "%]");
						}
					}
					previousPercent = percentComplete;

					// Percentage Display - END
				}

				lineBufferedReader.close();

				progressBar.setString("Complete !");

				if (percentageDisplayRequired) {
					removeLastLine();
					System.out.println();
				}

				System.out.println("No of Lines : " + noOfLines);

			} catch (Exception exception) {
				exception.printStackTrace();
				System.out.println("Error !");
			}

			return null;
		}

		@Override
		public void done() {
			stopWaiting();
			btnParseFile.setEnabled(true);
		}
	}

	private void keepWaiting() {
		resultTextPane.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
		toggleEnableButtons();
	}

	private void stopWaiting() {
		resultTextPane.setCursor(null);
		toggleEnableButtons();
	}

	private void toggleEnableButtons() {
		toggleEnable(btnDisplayDate, btnSelectFile, btnParseFile);
	}

	private void toggleEnable(Component... components) {
		for (Component currentComponent : components) {
			currentComponent.setEnabled(!currentComponent.isEnabled());
		}
	}

	private void printOnSameLine(String string) throws BadLocationException {
		removeLastLine();
		Document resultPaneDocument = resultTextPane.getDocument();
		resultPaneDocument.insertString(resultPaneDocument.getLength(), LINE_SEPARATOR + string, null);
		resultTextPane.setCaretPosition(resultTextPane.getDocument().getLength());
	}

	private void removeLastLine() throws BadLocationException {
		String content = resultTextPane.getDocument().getText(0, resultTextPane.getDocument().getLength());
		int lastLineBreak = content.lastIndexOf('\n');
		if (lastLineBreak == -1) {
			lastLineBreak = 0;
		}
		resultTextPane.getDocument().remove(lastLineBreak, resultTextPane.getDocument().getLength() - lastLineBreak);
	}

}

class ResultTextPaneOutputStream extends OutputStream {
	JTextPane resultTextPane;
	Document resultPaneDocument;

	public ResultTextPaneOutputStream(JTextPane resultTextPane) {
		this.resultTextPane = resultTextPane;
		this.resultPaneDocument = resultTextPane.getDocument();
	}

	@Override
	public void write(int b) throws IOException {
		try {

			resultPaneDocument.insertString(resultPaneDocument.getLength(), String.valueOf((char) b), null);
			resultTextPane.setCaretPosition(resultTextPane.getDocument().getLength());

		} catch (BadLocationException e) {
			e.printStackTrace();
		}

	}
}
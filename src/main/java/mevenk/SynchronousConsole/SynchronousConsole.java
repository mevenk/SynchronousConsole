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
import java.nio.file.Files;
import java.util.Date;
import java.util.concurrent.TimeUnit;

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

	private JButton btnPrintDate;

	private JButton btnSelectFile;
	private JLabel lblSelectedfile;
	private JButton btnParseFile;
	private JFileChooser fileChooser;
	private static File fileChooserCurrentDirectory = new File(SynchronousConsoleUtil.USER_HOME_DIRECTORY_PATH);

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
			System.out.println(LINE_SEPARATOR + "Select Text files only for accurate result.");

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

		btnPrintDate = new JButton("Print Date");
		btnPrintDate.setFont(new Font("Tahoma", Font.BOLD, 12));
		btnPrintDate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				System.out.println(new Date());
			}
		});
		btnPrintDate.setBounds(10, 11, 125, 30);
		frameSynchronousConsole.getContentPane().add(btnPrintDate);

		btnSelectFile = new JButton("File");
		btnSelectFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {

				fileChooser = new JFileChooser();
				fileChooser.setMultiSelectionEnabled(false);
				fileChooser.setCurrentDirectory(fileChooserCurrentDirectory);
				fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);

				int fileChooserReturnVal = fileChooser.showOpenDialog(frameSynchronousConsole);
				if (fileChooserReturnVal == JFileChooser.APPROVE_OPTION) {
					selectedFile = fileChooser.getSelectedFile();
					lblSelectedfile.setText(selectedFile.getName());
					lblSelectedfile.setToolTipText(selectedFile.getPath());
					fileChooserCurrentDirectory = fileChooser.getCurrentDirectory();
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

				if (selectedFile == null || !selectedFile.exists()) {
					System.out.println("Select a File !!");
					return;
				}

				System.out.println("Selected File : " + selectedFile.getPath());

				try {
					String mimeType = Files.probeContentType(selectedFile.toPath());
					if (mimeType == null || !mimeType.contains("text")) {
						if (JOptionPane.showConfirmDialog(frameSynchronousConsole,
								"MIME Type : " + mimeType + LINE_SEPARATOR + "Proceed ?", selectedFile.getName(),
								JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE) != JOptionPane.YES_OPTION) {
							return;
						}
					}
				} catch (Exception exception) {
					System.out.println("ERROR !!" + LINE_SEPARATOR);
					exception.printStackTrace();
					return;
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

	class FileParseInBackground extends SwingWorker<Long, Void> {

		@Override
		protected Long doInBackground() throws Exception {

			keepWaiting();

			double bytes = selectedFile.length();

			double size = 0;
			String unitType = "";

			if (bytes < 1024) {
				size = bytes;
				unitType = "B";
			} else if (bytes < 1048576) {
				size = bytes / 1204;
				unitType = "KB";
			} else if (bytes < 1073741824) {
				size = bytes / 1024 / 1024;
				unitType = "MB";
			} else {
				size = bytes / 1024 / 1024 / 1024;
				unitType = "GB";
			}

			System.out.println("Size : " + Math.floor(size) + " " + unitType);

			printRemovableLine("Counting Lines...");

			int noOfLines = SynchronousConsoleUtil.lineCount(selectedFile);
			removeLastLine();
			System.out.println();

			boolean percentageDisplayRequired = false;

			if (noOfLines != -1) {
				percentageDisplayRequired = true;
				System.out.println("No of Lines : " + noOfLines);
			} else {
				System.out.println("Line Count Error !!");
			}

			int counter = 0;
			int percentComplete = 0;
			int previousPercent = 0;

			long startTime = new Date().getTime();

			BufferedReader lineBufferedReader = new BufferedReader(new FileReader(selectedFile));

			while (lineBufferedReader.readLine() != null) {

				percentComplete = ((++counter) * 100) / noOfLines;

				// Percentage Display

				if (percentComplete != previousPercent) {
					progressBar.setValue((int) percentComplete);
					if (percentageDisplayRequired) {
						printRemovableLine("Please Wait [" + percentComplete + "%]");
					}
				}
				previousPercent = percentComplete;

				// Percentage Display - END
			}

			long endTime = new Date().getTime();

			lineBufferedReader.close();

			if (percentageDisplayRequired) {
				removeLastLine();
			}

			return endTime - startTime;
		}

		@Override
		public void done() {
			System.out.println();
			try {
				long timeTakenInMillis = get();
				progressBar.setString("Complete !");
				System.out.println("Time Taken : " + TimeUnit.MILLISECONDS.toSeconds(timeTakenInMillis) + " sec ("
						+ timeTakenInMillis + " ms)");
			} catch (Exception exception) {
				progressBar.setValue(0);
				System.out.println("Error !!");
				exception.printStackTrace();
			}
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
		toggleEnable(btnPrintDate, btnSelectFile, btnParseFile);
	}

	private void toggleEnable(Component... components) {
		for (Component currentComponent : components) {
			currentComponent.setEnabled(!currentComponent.isEnabled());
		}
	}

	private void printRemovableLine(String string) throws BadLocationException {
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
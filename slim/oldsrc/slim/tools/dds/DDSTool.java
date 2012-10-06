package slim.tools.dds;

import gr.zdimensions.jsquish.Squish;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FlowLayout;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingConstants;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;

import net.iharder.dnd.FileDrop;

import org.lwjgl.BufferUtils;

import slim.texture.io.DDSDecoder;

public class DDSTool extends JFrame {

	static final Preferences prefs = Preferences.userNodeForPackage(DDSTool.class);
	static final Object IMAGE_VIEWER_INTERPOLATION = RenderingHints.VALUE_INTERPOLATION_BICUBIC;
	
	
	private BufferedImage checkImage; 
	private Dimension defaultViewportMinSize = new Dimension(128, 128);
	private JPanel contentPane;
	private Border defaultContentBorder;
	private final JMenuBar menuBar = new JMenuBar();
	private final JMenu mnFile = new JMenu("File");
	private final JMenuItem mntmOpenImage = new JMenuItem("Open Image");
	private final JMenuItem mntmExit = new JMenuItem("Exit");
	
	enum OutFormats {
		DXT1(DDSDecoder.Format.RGB_DXT1, Squish.CompressionType.DXT1),
		DXT1A(DDSDecoder.Format.RGBA_DXT1, Squish.CompressionType.DXT1),
		DXT3(DDSDecoder.Format.RGBA_DXT3, Squish.CompressionType.DXT3),
		DXT5(DDSDecoder.Format.RGBA_DXT5, Squish.CompressionType.DXT5);
		
		public final DDSDecoder.Format format;
		public final Squish.CompressionType compression;
		
		OutFormats(DDSDecoder.Format format, Squish.CompressionType compression) {
			this.format = format;
			this.compression = compression;
		}
		
		public boolean isDXTn() {
			return true;
		}
	}
	
	private static final String DEFAULT_INFO_LABEL = "No image loaded...";
	private String infoOriginalStr;
	final String INFO_FROM_FILE = "{0} ({1}x{2}){3}{4}";
	
	private final String[] ZOOM_TEXT = new String[] {
		"Fit", "25%", "50%", "100%", "125%", "250%"
	};
	private final float[] ZOOM_VALUES = new float[] {
		0f, .25f, .5f, 1f, 1.25f, 2.5f
	};
	
	private final int ZOOM_DEFAULT_INDEX = 3;
	
	private class PreviewPane extends JComponent implements ComponentListener {
		
		private BufferedImage image, compressed;
		private boolean zoomFit = false;
		private float zoom = 1f;
		private boolean checkBG = false, showCompressed = false;
		
		public PreviewPane() {
			addComponentListener(this);
		}
		
		public void setShowCompressed(boolean compressed) {
			this.showCompressed = compressed;
			updateImage();
		}
		
		public boolean isShowCompressed() {
			return showCompressed;
		}
		
		public void setZoom(float zoom) {
			this.zoom = zoom;
			zoomFit = false;
			updateSize(minSizeOrImgSize());
			updateImage();
		}
		
		public void setCheckeredBackground(boolean show) {
			this.checkBG = show;
			updateImage();
		}
		
		public boolean isCheckeredBackground() {
			return checkBG;
		}
		
		void fit() {
			this.zoom = Math.min(getWidth() / (float)image.getWidth(), 
					getHeight()/(float)image.getHeight());
		}
		
		public void setZoomFit() {
			zoomFit = true;
			if (image==null)
				this.zoom = 0f;
			else
				fit();
			updateSize(defaultViewportMinSize);
			updateImage();
		}
		
		void updateSize(Dimension min) {
			setPreferredSize(min);
			setMinimumSize(min);
			setSize(min);
		}
		
		public void updateImage() {
			invalidate();
			repaint();
		}
		
		Dimension minSizeOrImgSize() {
			return image==null ? defaultViewportMinSize 
					: new Dimension((int)(image.getWidth()*zoom), 
							(int)(image.getHeight()*zoom));
		}
		
		public void setImage(BufferedImage image) {
			this.compressed = null;
			this.image = image;
			if (image!=null && (zoom==0 || zoomFit))
				fit();
			updateSize(minSizeOrImgSize());
			updateImage();
		}
		
		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			
			if (checkBG) {
				BufferedImage checkImage = getCheckImage();
				int tW = checkImage.getWidth();
				int tH = checkImage.getHeight();
				int nHTiles = getWidth()/tW;
				int nVTiles = getHeight()/tH;
				if (tW*nHTiles < getWidth())
					nHTiles++;
				if (tH*nVTiles < getHeight())
					nVTiles++;
				for (int x=0; x<nHTiles; x++) {
					for (int y=0; y<nVTiles; y++) {
						g.drawImage(checkImage, x*tW, y*tH, tW, tH, null);
					}
				}
			}
			
			Graphics2D g2d = (Graphics2D)g;
			g2d.setRenderingHint(RenderingHints.KEY_INTERPOLATION, IMAGE_VIEWER_INTERPOLATION);
			BufferedImage image = showCompressed ? this.compressed : this.image;
			if (image!=null) {
				int w = image.getWidth();
				int h = image.getHeight();
				if (zoomFit) 
					fit();
				OutFormats dx = OutFormats.values()[compressionBox.getSelectedIndex()];
//				if (showCompressed && dx == OutFormats.DXT1)
//					g.fillRect(0, 0, (int)(w*zoom), (int)(h*zoom));
				g.drawImage(image, 0, 0, (int)(w*zoom), (int)(h*zoom), null);
			}
		}

		public void componentHidden(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void componentMoved(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}

		public void componentResized(ComponentEvent arg0) {
//			System.out.println("resized");
//			float old = this.zoom;
//			if (zoomFit)
//				fit();
//			if (this.zoom != old)
//				repaint();
		}

		public void componentShown(ComponentEvent arg0) {
			// TODO Auto-generated method stub
			
		}
	}
	
	private PreviewPane previewImage = new PreviewPane(); 
	private final List<String> IMAGEIO_FORMATS = new ArrayList<String>();
	private final JScrollPane scrollPane = new JScrollPane();
	private final JComboBox comboBox = new JComboBox();
	private final JMenu mnView = new JMenu("View");
	private final JCheckBoxMenuItem checkBGItem = new JCheckBoxMenuItem("Checkered Background");
	private final JCheckBox previewCompressionBox = new JCheckBox("Preview Compression");
	private final JLabel infoLabel = new JLabel("New label");
	private final JPanel panel_1 = new JPanel();
	private final JLabel label = new JLabel("Output:");
	private final JTextField outputField = new JTextField();
	private final JButton button = new JButton("");
	private final JCheckBox premultCheck = new JCheckBox("Premultiply Alpha");
	private final JComboBox compressionBox = new JComboBox();
	private final JLabel label_1 = new JLabel("Compression:");
	private final JComboBox formatBox = new JComboBox();
	private final JLabel label_2 = new JLabel("Format:");
	private final JPanel panel_2 = new JPanel();
	private final JButton button_1 = new JButton("Save as .DDS");
	
	private byte[] rgbaData;
	private byte[] compressedData;

	private static int translate(byte b) {
		if (b < 0) {
			return 256 + b;
		}
		return b;
	}
	
	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		
		
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					DDSTool frame = new DDSTool();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}
	
	

	/**
	 * Create the frame.
	 */
	public DDSTool() {
		button.setIcon(new ImageIcon(DDSTool.class.getResource("/res/tools/folder_explore.png")));
		label_1.setHorizontalAlignment(SwingConstants.TRAILING);
		outputField.setColumns(10);
		label.setHorizontalAlignment(SwingConstants.TRAILING);
		comboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int index = comboBox.getSelectedIndex();
				if (index==0) {
					previewImage.setZoomFit();
				} else if (index>0 && index<ZOOM_VALUES.length){
					float z = ZOOM_VALUES[index];
					previewImage.setZoom(z);
				}
			}
		});
		comboBox.setModel(new DefaultComboBoxModel(ZOOM_TEXT));
		comboBox.setSelectedIndex(ZOOM_DEFAULT_INDEX);
		for (String s : ImageIO.getReaderFormatNames())
			if (!IMAGEIO_FORMATS.contains(s.toLowerCase()))
				IMAGEIO_FORMATS.add(s.toLowerCase());
		initGUI();
	}
	
	public BufferedImage getCheckImage() {
		if (checkImage==null)
			checkImage = createCheckeredBackground(16, 512);
		return checkImage;
	}
	
	private BufferedImage createCheckeredBackground(int ts, int size) {
		BufferedImage img = new BufferedImage(size, size, BufferedImage.TYPE_INT_RGB);
		Graphics g = img.getGraphics();
		int tSize = ts;
		int nHTiles = img.getWidth()/tSize + 1;
		int nVTiles = img.getHeight()/tSize + 1;
		for (int x=0; x<nHTiles; x++) {
			for (int y=0; y<nVTiles; y++) {
				Color c = ((x+y) % 2 == 0) ? Color.lightGray : Color.white;
				g.setColor(c);
				g.fillRect(x*tSize, y*tSize, tSize, tSize);
			}
		}
		g.dispose();
		return img;
	}
	
	
	void closeImage() {
		rgbaData = compressedData = null;
		previewImage.image = null;
		previewImage.compressed = null;
		infoLabel.setText(DEFAULT_INFO_LABEL);
	}
	
	private void initGUI() {
		infoLabel.setHorizontalAlignment(SwingConstants.TRAILING);
		infoLabel.setText(DEFAULT_INFO_LABEL);
		formatBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkUpdateCompression();
			}
		});
		formatBox.setModel(new DefaultComboBoxModel(OutFormats.values()));
		compressionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkUpdateCompression();
			}
		});
		
		compressionBox.setModel(new DefaultComboBoxModel(Squish.CompressionMethod.values()));
		
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent arg0) {
				prefs.putInt("x", getX());
				prefs.putInt("y", getY());
				prefs.putInt("width", getWidth());
				prefs.putInt("height", getHeight());
			}
		});
		setTitle("DDS Converter");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(699, 543);
		checkBGItem.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				previewImage.setCheckeredBackground(!previewImage.isCheckeredBackground());
			}
		});
		checkBGItem.setSelected(prefs.getBoolean("checkBG", true));
		previewImage.setCheckeredBackground(checkBGItem.isSelected());
		
		String x = prefs.get("x", null);
		String y = prefs.get("y", null);
		if (x==null||y==null) {
			setLocationRelativeTo(null);
		} else {
			try {
				setLocation(Integer.parseInt(x), Integer.parseInt(y));
			} catch (NumberFormatException e) { 
				setLocationRelativeTo(null);
			}
		}
		
		setJMenuBar(menuBar);
		
		menuBar.add(mnFile);
		
		mnFile.add(mntmOpenImage);
		mntmExit.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				exit();
			}
		});
		
		mnFile.add(mntmExit);
		
		menuBar.add(mnView);
		
		mnView.add(checkBGItem);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		
		initFileDrop();
		
		scrollPane.setBackground(null);
		scrollPane.setOpaque(false);
		scrollPane.setMinimumSize(new Dimension(scrollPane.getMinimumSize().width, 128));
		scrollPane.getViewport().setBackground(null);
		scrollPane.setViewportView(previewImage);
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(
			gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
						.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createSequentialGroup()
							.addComponent(previewCompressionBox)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(infoLabel, GroupLayout.DEFAULT_SIZE, 457, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(panel_2, GroupLayout.DEFAULT_SIZE, 677, Short.MAX_VALUE))
					.addContainerGap())
		);
		previewCompressionBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (previewImage.compressed==null)
					checkUpdateCompression();
				previewImage.setShowCompressed(!previewImage.isShowCompressed());
			}
		});
		gl_contentPane.setVerticalGroup(
			gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_contentPane.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
						.addComponent(comboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(previewCompressionBox)
						.addComponent(infoLabel))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(scrollPane, GroupLayout.DEFAULT_SIZE, 318, Short.MAX_VALUE)
					.addPreferredGap(ComponentPlacement.RELATED)
					.addComponent(panel_2, GroupLayout.PREFERRED_SIZE, 120, GroupLayout.PREFERRED_SIZE)
					.addContainerGap())
		);
		FlowLayout flowLayout = (FlowLayout) panel_2.getLayout();
		flowLayout.setAlignment(FlowLayout.CENTER);
		flowLayout.setHgap(0);
		flowLayout.setVgap(0);
		GroupLayout gl_panel_1 = new GroupLayout(panel_1);
		gl_panel_1.setHorizontalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addComponent(label, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(outputField, GroupLayout.DEFAULT_SIZE, 555, Short.MAX_VALUE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(button, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE))
						.addGroup(gl_panel_1.createSequentialGroup()
							.addComponent(label_2, GroupLayout.PREFERRED_SIZE, 48, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(formatBox, GroupLayout.PREFERRED_SIZE, 147, GroupLayout.PREFERRED_SIZE)
							.addPreferredGap(ComponentPlacement.RELATED)
							.addComponent(label_1, GroupLayout.PREFERRED_SIZE, 87, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(compressionBox, GroupLayout.PREFERRED_SIZE, 184, GroupLayout.PREFERRED_SIZE)
							.addGap(6)
							.addComponent(premultCheck, GroupLayout.PREFERRED_SIZE, 143, GroupLayout.PREFERRED_SIZE))
						.addComponent(button_1, Alignment.TRAILING, GroupLayout.PREFERRED_SIZE, 125, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		premultCheck.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				checkUpdateCompression();
			}
		});
		gl_panel_1.setVerticalGroup(
			gl_panel_1.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panel_1.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createParallelGroup(Alignment.BASELINE)
							.addComponent(label)
							.addComponent(outputField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
						.addComponent(button, GroupLayout.PREFERRED_SIZE, 28, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
						.addGroup(gl_panel_1.createSequentialGroup()
							.addGroup(gl_panel_1.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_panel_1.createSequentialGroup()
									.addGap(5)
									.addComponent(label_2))
								.addGroup(gl_panel_1.createSequentialGroup()
									.addGap(5)
									.addComponent(label_1))
								.addComponent(compressionBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addGroup(gl_panel_1.createSequentialGroup()
									.addGap(1)
									.addComponent(premultCheck)))
							.addPreferredGap(ComponentPlacement.RELATED, 9, Short.MAX_VALUE)
							.addComponent(button_1))
						.addComponent(formatBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
					.addContainerGap())
		);
		panel_2.add(panel_1);
		panel_1.setLayout(gl_panel_1);
		contentPane.setLayout(gl_contentPane);
	}
	
	private void initFileDrop() {
		defaultContentBorder = contentPane.getBorder();
		Insets i = defaultContentBorder.getBorderInsets(contentPane);
		Color color = new Color(0f, 0f, 1f, 0.25f);
		new FileDrop(contentPane,
				BorderFactory.createMatteBorder(i.top, i.left, i.bottom, i.right, color), 
				new FileDrop.Listener() {
			public void filesDropped(File[] files) {
				contentPane.setBorder(defaultContentBorder);
				try {
					openImages(files);
				} catch (IOException e) {
					JOptionPane.showMessageDialog(DDSTool.this, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					System.err.println(e.getMessage());
					infoLabel.setText(DEFAULT_INFO_LABEL);
				}
			}
		});
	}
	
	void updateLabel(String msg, Object ... vars) {
		infoLabel.setText(MessageFormat.format(msg, vars));
	}
	
	void checkUpdateCompression() {
		if (!previewCompressionBox.isSelected() || previewImage.image==null)
			return;
		
		System.out.println("Updating compression");
		
		Squish.CompressionMethod method = Squish.CompressionMethod.values()[compressionBox.getSelectedIndex()];
		OutFormats fmt = OutFormats.values()[formatBox.getSelectedIndex()];
		DDSDecoder.Format ddsFmt = fmt.format;
		if (fmt.isDXTn()) {
			int width = previewImage.image.getWidth();
			int height = previewImage.image.getHeight();
			Squish.CompressionType type = fmt.compression;
			
			byte[] tempBlocks = Squish.compressImage(rgbaData, width, height, null, type, method, 
					Squish.CompressionMetric.PERCEPTUAL, premultCheck.isSelected() );
			previewImage.compressed = decompress(width, height, tempBlocks, fmt, fmt!=OutFormats.DXT1);
			previewImage.updateImage();
		} else {
			System.out.println("Not yet supported");
		}
	}
	
	private void openImages(File[] in) throws IOException {
		if (in.length==0)
			return;
		
		File f = in[0];
		String path = f.getPath();
		if (endsWithIgnoreCase(path, ".dds")) {
			FileInputStream fin = null;
			try {
				fin = new FileInputStream(f);
				BufferedImage img = loadDDS(f.getName(), fin);
				openImage(img);
				previewImage.setImage(img);
				checkUpdateCompression();
				//previewCompressionBox.setSelected(false);
			} finally {
				try { 
					if (fin!=null)
						fin.close(); 
				} catch (IOException e) {}
			}
		} else {
			int i = path.lastIndexOf('.');
			if (i==-1 || i==path.length()-1)
				return;
			String ext = path.substring(i+1);
			if (IMAGEIO_FORMATS.contains(ext.toLowerCase())) {
				BufferedImage img = ImageIO.read(f);
				openImage(img);
				updateLabel(INFO_FROM_FILE, f.getName(), img.getWidth(), img.getHeight(), "", "");
				previewImage.setImage(img);
				checkUpdateCompression();
				//previewCompressionBox.setSelected(false);
			}
		}
	}
	
	void openImage(BufferedImage image) {
		if (image.getType()!=BufferedImage.TYPE_4BYTE_ABGR) {
			BufferedImage c = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_4BYTE_ABGR);
			Graphics g = c.createGraphics();
			g.drawImage(image, 0, 0, null);
			g.dispose();
			image = c;
		}
		byte[] tmp = ((DataBufferByte)image.getRaster().getDataBuffer()).getData();
		
		//make a copy of the data so we can swizzle it
		rgbaData = new byte[tmp.length];
		//swizzle
		for (int i=0; i<tmp.length; i+=4) {
			byte a = tmp[i+0];
			byte b = tmp[i+1];
			byte g = tmp[i+2];
			byte r = tmp[i+3];
			rgbaData[i+0] = r;
			rgbaData[i+1] = g;
			rgbaData[i+2] = b;
			rgbaData[i+3] = a;
		}
		
		//null for now
		previewImage.compressed = null;
	}
	
	BufferedImage decompress(int width, int height, byte[] blocks, OutFormats fmt, boolean alpha) {
		byte[] imgData = Squish.decompressImage(null, width, height, blocks, fmt.compression);
		//swizzle RGBA -> ABGR
		for (int i=0; i<imgData.length; i+=4) {
			byte r = imgData[i+0];
			byte g = imgData[i+1];
			byte b = imgData[i+2];
			byte a = imgData[i+3];
			imgData[i+0] = alpha ? a : (byte)0xFF;
			imgData[i+1] = b;
			imgData[i+2] = g;
			imgData[i+3] = r;
		}
		BufferedImage img = new BufferedImage(width, height, BufferedImage.TYPE_4BYTE_ABGR);
		byte[] b = ((DataBufferByte)img.getRaster().getDataBuffer()).getData();
		System.arraycopy(imgData, 0, b, 0, imgData.length);
		return img;
	}
	
	BufferedImage loadDDS(String ref, InputStream in) throws IOException {
		DDSDecoder dds = new DDSDecoder(in);
		int width = dds.getWidth();
		int height = dds.getHeight();
		int bpp = dds.getFormat().getBytesPerPixel();
		ByteBuffer blocksBuffer = BufferUtils.createByteBuffer(dds.getSize());
		dds.decode(blocksBuffer);
		blocksBuffer.flip();
		byte[] blocks = new byte[blocksBuffer.capacity()];
		blocksBuffer.get(blocks);
		
		OutFormats type;
		switch (dds.getFormat()) {
		case RGB_DXT1:
			type = OutFormats.DXT1; break;
		case RGBA_DXT1:
			type = OutFormats.DXT1A; break;
		case RGBA_DXT3:
			type = OutFormats.DXT3; break;
		case RGBA_DXT5:
			type = OutFormats.DXT5; break;
		default:
			throw new IOException("invalid compression format");
		}
		BufferedImage img = decompress(width, height, blocks, type, type!=OutFormats.DXT1);
		updateDDSInfo(ref, dds);
		return img;
	}
	
	void updateDDSInfo(String ref, DDSDecoder dds) {
		int width = dds.getWidth();
		int height = dds.getHeight();
		//String mips = dds.getMipMapCount()>1 ? " (mipmapped)" : "";
		updateLabel(INFO_FROM_FILE, ref, width, height, " - "+dds.getFormat().name(), "");
	}
	
	byte[] rgb2rgba(byte[] rgb) {
		byte[] rgba = new byte[rgb.length/4 * 3];
		for (int i=0, j=0; i<rgba.length;) {
			rgba[i++] = rgb[j++];
			rgba[i++] = rgb[j++];
			rgba[i++] = rgb[j++];
			rgba[i++] = (byte)0xFF;
		}
		return rgba;
	}
	
	BufferedImage createImage(ByteBuffer buf, int width, int height) {
		return null;
	}
	

    public final static boolean endsWithIgnoreCase(String str, String end) {
        return str.regionMatches(true, str.length()-
                end.length(), end, 0, end.length());
    }    

	public void exit() {
		System.exit(0);
	}
}

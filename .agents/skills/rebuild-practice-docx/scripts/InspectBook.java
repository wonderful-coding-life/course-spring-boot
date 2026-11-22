import java.io.*;
import java.nio.file.*;
import java.nio.charset.StandardCharsets;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;
class InspectBook {
    static class Audit extends PDFTextStripper {
        int overflow=0;
        Audit() throws IOException {super();}
        protected void processTextPosition(TextPosition p) {
            if (!p.getUnicode().isBlank() && (p.getXDirAdj() < 20 || p.getXDirAdj()+p.getWidthDirAdj()>594 || p.getYDirAdj()>777)) { overflow++; System.out.println("Outlying glyph on page "+getCurrentPageNo()+": "+p.getUnicode()+" at "+p.getXDirAdj()+","+p.getYDirAdj()); }
            super.processTextPosition(p);
        }
    }
    public static void main(String[] args) throws Exception {
        Path out=Path.of(args[1]); Files.createDirectories(out);
        try(var doc=Loader.loadPDF(new File(args[0]))) {
            var renderer=new PDFRenderer(doc);
            var audit=new Audit();
            var all=new StringBuilder(); var stats=new StringBuilder();
            int count=doc.getNumberOfPages();
            for (int i=0;i<count;i++) {
                audit.setStartPage(i+1);audit.setEndPage(i+1);
                String text=audit.getText(doc);
                all.append("\n=== PHYSICAL PAGE "+(i+1)+" ===\n").append(text);
                stats.append((i+1)+"\t"+text.strip().length()+"\t"+text.lines().count()+"\n");
                var image=renderer.renderImageWithDPI(i,144);
                ImageIO.write(image,"png",out.resolve(String.format("page-%03d.png",i+1)).toFile());
                if ((i+1)%20==0) System.out.println("Rendered "+(i+1)+" / "+count);
            }
            for(int start=0;start<count;start+=9) {
                BufferedImage sheet=new BufferedImage(1050,1410,BufferedImage.TYPE_INT_RGB);
                var g=sheet.createGraphics();g.setColor(new Color(222,227,223));g.fillRect(0,0,1050,1410);
                g.setRenderingHint(RenderingHints.KEY_INTERPOLATION,RenderingHints.VALUE_INTERPOLATION_BICUBIC);
                g.setFont(new Font("SansSerif",Font.BOLD,14));
                for(int k=0;k<9 && start+k<count;k++) {
                    var img=ImageIO.read(out.resolve(String.format("page-%03d.png",start+k+1)).toFile());
                    int x=(k%3)*350+8;int y=(k/3)*470+24;
                    g.drawImage(img,x,y,334,432,null);g.setColor(new Color(32,62,42));g.drawString("PAGE "+(start+k+1),x,y-6);
                }
                g.dispose();ImageIO.write(sheet,"png",out.resolve(String.format("sheet-%02d.png",start/9+1)).toFile());
            }
            Files.writeString(out.resolve("all-text.txt"),all.toString(),StandardCharsets.UTF_8);
            Files.writeString(out.resolve("page-stats.tsv"),stats.toString(),StandardCharsets.UTF_8);
            Files.writeString(out.resolve("layout-audit.json"),"{\"Pages\":"+count+",\"OutlyingGlyphs\":"+audit.overflow+",\"Dpi\":144}",StandardCharsets.UTF_8);
            System.out.println("Pages "+count+"; out-of-page text glyphs "+audit.overflow);
        }
    }
}

package WGK.controller;

import WGK.domain.Pedido;
import WGK.domain.PedidoDetalle;
import WGK.domain.Sneaker;
import WGK.repository.PedidoRepository;
import WGK.service.SneakerService;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import java.awt.Color;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/carrito")
public class CarritoController {

    @Autowired
    private SneakerService sneakerService;

    @Autowired
    private PedidoRepository pedidoRepository;

    @GetMapping("/ver")
    public String verCarrito(HttpSession session, Model model) {
        List<Sneaker> carrito = getCarrito(session);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", calcularTotal(carrito));
        return "/carrito/ver";
    }

    @PostMapping("/agregar/{idSneaker}")
    public String agregar(@PathVariable Integer idSneaker, HttpSession session) {
        Sneaker sneaker = sneakerService.getSneaker(idSneaker);
        if (sneaker != null) getCarrito(session).add(sneaker);
        return "redirect:/";
    }

    @PostMapping("/eliminar/{index}")
    public String eliminar(@PathVariable Integer index, HttpSession session) {
        List<Sneaker> carrito = getCarrito(session);
        if (index >= 0 && index < carrito.size()) carrito.remove((int) index);
        return "redirect:/carrito/ver";
    }

    @PostMapping("/vaciar")
    public String vaciar(HttpSession session) {
        session.removeAttribute("carrito");
        return "redirect:/carrito/ver";
    }

    // ── Historial de pedidos del usuario logueado ─────────────────────────
    @GetMapping("/historial")
    public String historial(Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getPrincipal())) {
            return "redirect:/login";
        }
        String usuario = auth.getName();
        model.addAttribute("pedidos", pedidoRepository.findByUsuarioOrderByFechaDesc(usuario));
        model.addAttribute("usuario", usuario);
        return "/carrito/historial";
    }

    // ── Pagar: guarda pedido en BD, vacía carrito, genera PDF ─────────────
    @PostMapping("/pagar")
    public String pagar(HttpSession session,
                        HttpServletResponse response,
                        RedirectAttributes ra) throws Exception {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        if (!autenticado) {
            ra.addFlashAttribute("errorPago", "Debes iniciar sesión para completar tu compra.");
            return "redirect:/carrito/ver";
        }

        List<Sneaker> carrito = getCarrito(session);
        if (carrito == null || carrito.isEmpty()) {
            ra.addFlashAttribute("errorPago", "Tu carrito está vacío.");
            return "redirect:/carrito/ver";
        }

        BigDecimal total    = calcularTotal(carrito);
        String usuario      = auth.getName();
        LocalDateTime ahora = LocalDateTime.now();
        String fechaHora    = ahora.format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String numFactura   = "WGK-" + System.currentTimeMillis();

        // ── Guardar pedido en BD ──
        Pedido pedido = new Pedido();
        pedido.setNumero(numFactura);
        pedido.setUsuario(usuario);
        pedido.setFecha(ahora);
        pedido.setTotal(total);

        List<PedidoDetalle> detalles = new ArrayList<>();
        for (Sneaker s : carrito) {
            PedidoDetalle d = new PedidoDetalle();
            d.setPedido(pedido);
            d.setDescripcion(s.getDescripcion());
            d.setMarca(s.getMarca() != null ? s.getMarca().getDescripcion() : "—");
            d.setPrecio(s.getPrecio());
            detalles.add(d);
        }
        pedido.setDetalles(detalles);
        pedidoRepository.save(pedido);

        // ── Vaciar carrito ──
        List<Sneaker> carritoParaPDF = new ArrayList<>(carrito);
        session.removeAttribute("carrito");

        // ── Generar PDF ──
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"factura_" + numFactura + ".pdf\"");

        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        Color rojo     = new Color(220, 0, 0);
        Color grisOsc  = new Color(40, 40, 40);
        Color grisCla  = new Color(245, 245, 245);
        Color lineaCol = new Color(220, 220, 220);

        Font fNormal   = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font fBold     = new Font(Font.HELVETICA, 11, Font.BOLD, Color.DARK_GRAY);
        Font fRojo     = new Font(Font.HELVETICA, 10, Font.BOLD, rojo);
        Font fTabHead  = new Font(Font.HELVETICA, 9,  Font.BOLD, Color.WHITE);
        Font fTabCell  = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font fPeq      = new Font(Font.HELVETICA, 8,  Font.NORMAL, Color.GRAY);
        Font fLogo     = new Font(Font.HELVETICA, 32, Font.BOLD, rojo);
        Font fTitle    = new Font(Font.HELVETICA, 18, Font.BOLD, grisOsc);

        // Encabezado
        PdfPTable hdr = new PdfPTable(2);
        hdr.setWidthPercentage(100); hdr.setWidths(new float[]{1f, 2f}); hdr.setSpacingAfter(20);
        PdfPCell cLogo = new PdfPCell(new Paragraph("WGK.", fLogo));
        cLogo.setBorder(PdfPCell.NO_BORDER); cLogo.setPaddingBottom(8); hdr.addCell(cLogo);
        Paragraph info = new Paragraph();
        info.add(new Chunk("WegoTKicks PR\n", fBold));
        info.add(new Chunk("Tienda Certificada de Sneakers\n", fNormal));
        info.add(new Chunk("Puerto Rico · Tel: 3244-2144", fNormal));
        PdfPCell cInfo = new PdfPCell(info);
        cInfo.setBorder(PdfPCell.NO_BORDER); cInfo.setHorizontalAlignment(Element.ALIGN_RIGHT); hdr.addCell(cInfo);
        doc.add(hdr);

        // Línea roja
        PdfPTable lr = new PdfPTable(1); lr.setWidthPercentage(100); lr.setSpacingAfter(16);
        PdfPCell cr = new PdfPCell(new Phrase(" "));
        cr.setBackgroundColor(rojo); cr.setBorder(PdfPCell.NO_BORDER); cr.setFixedHeight(3f); lr.addCell(cr);
        doc.add(lr);

        doc.add(new Paragraph("FACTURA DE COMPRA", fTitle));
        doc.add(new Paragraph(" "));

        // Datos factura
        PdfPTable df = new PdfPTable(2); df.setWidthPercentage(100); df.setSpacingAfter(20);
        Paragraph clie = new Paragraph();
        clie.add(new Chunk("FACTURADO A\n", fRojo));
        clie.add(new Chunk("Cliente: " + usuario + "\n", fNormal));
        clie.add(new Chunk("Envío: Gratis · Verificación: Incluida", fNormal));
        PdfPCell cc = new PdfPCell(clie); cc.setBorder(PdfPCell.NO_BORDER); df.addCell(cc);
        Paragraph fact = new Paragraph();
        fact.add(new Chunk("DETALLES\n", fRojo));
        fact.add(new Chunk("Nº: " + numFactura + "\n", fNormal));
        fact.add(new Chunk("Fecha: " + fechaHora + "\n", fNormal));
        fact.add(new Chunk("Estado: Pagado", fNormal));
        PdfPCell cf = new PdfPCell(fact); cf.setBorder(PdfPCell.NO_BORDER);
        cf.setHorizontalAlignment(Element.ALIGN_RIGHT); df.addCell(cf);
        doc.add(df);

        // Tabla productos
        PdfPTable tb = new PdfPTable(4);
        tb.setWidthPercentage(100); tb.setWidths(new float[]{3f, 1.5f, 1f, 1.2f}); tb.setSpacingAfter(20);
        for (String h : new String[]{"PRODUCTO", "MARCA", "CANT.", "PRECIO"}) {
            PdfPCell ch = new PdfPCell(new Phrase(h, fTabHead));
            ch.setBackgroundColor(grisOsc); ch.setPadding(8); ch.setBorder(PdfPCell.NO_BORDER); tb.addCell(ch);
        }
        boolean alt = false;
        for (Sneaker s : carritoParaPDF) {
            Color bg = alt ? grisCla : Color.WHITE;
            celdaT(tb, s.getDescripcion(), fTabCell, bg, lineaCol, Element.ALIGN_LEFT);
            celdaT(tb, s.getMarca() != null ? s.getMarca().getDescripcion() : "—", fTabCell, bg, lineaCol, Element.ALIGN_LEFT);
            celdaT(tb, "1", fTabCell, bg, lineaCol, Element.ALIGN_CENTER);
            celdaT(tb, "$" + s.getPrecio(), fTabCell, bg, lineaCol, Element.ALIGN_RIGHT);
            alt = !alt;
        }
        doc.add(tb);

        // Totales
        PdfPTable tot = new PdfPTable(2);
        tot.setWidthPercentage(42); tot.setHorizontalAlignment(Element.ALIGN_RIGHT); tot.setSpacingAfter(30);
        filaTot(tot, "Subtotal:", "$" + total, fNormal, Color.WHITE, lineaCol);
        filaTot(tot, "Envío:", "Gratis", fNormal, Color.WHITE, lineaCol);
        filaTot(tot, "Verificación:", "Incluida", fNormal, Color.WHITE, lineaCol);
        PdfPCell sep = new PdfPCell(new Phrase(" "));
        sep.setColspan(2); sep.setBackgroundColor(lineaCol); sep.setFixedHeight(1f); sep.setBorder(PdfPCell.NO_BORDER); tot.addCell(sep);
        PdfPCell tl = new PdfPCell(new Phrase("TOTAL:", fTabHead));
        tl.setBackgroundColor(rojo); tl.setPadding(8); tl.setBorder(PdfPCell.NO_BORDER); tot.addCell(tl);
        PdfPCell tv = new PdfPCell(new Phrase("$" + total, fTabHead));
        tv.setBackgroundColor(rojo); tv.setPadding(8); tv.setHorizontalAlignment(Element.ALIGN_RIGHT); tv.setBorder(PdfPCell.NO_BORDER); tot.addCell(tv);
        doc.add(tot);

        // Pie
        PdfPTable pie = new PdfPTable(1); pie.setWidthPercentage(100);
        PdfPCell cp = new PdfPCell();
        cp.setBorderColor(lineaCol); cp.setBorderWidth(0.5f); cp.setBorder(PdfPCell.TOP); cp.setPaddingTop(10);
        Paragraph tp = new Paragraph();
        tp.add(new Chunk("¡Gracias por tu compra en WegoTKicks PR!\n", fBold));
        tp.add(new Chunk("Todos nuestros pares son 100% verificados. Contacto: 3244-2144 · Puerto Rico", fPeq));
        tp.setAlignment(Element.ALIGN_CENTER);
        cp.addElement(tp); pie.addCell(cp); doc.add(pie);

        doc.close();
        return null;
    }

    // ── Helpers privados ──────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private List<Sneaker> getCarrito(HttpSession session) {
        List<Sneaker> c = (List<Sneaker>) session.getAttribute("carrito");
        if (c == null) { c = new ArrayList<>(); session.setAttribute("carrito", c); }
        return c;
    }

    private BigDecimal calcularTotal(List<Sneaker> carrito) {
        return carrito.stream().map(Sneaker::getPrecio).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void celdaT(PdfPTable t, String txt, Font f, Color bg, Color border, int align) {
        PdfPCell c = new PdfPCell(new Phrase(txt, f));
        c.setBackgroundColor(bg); c.setPadding(8); c.setBorderColor(border);
        c.setBorderWidth(0.5f); c.setHorizontalAlignment(align); t.addCell(c);
    }

    private void filaTot(PdfPTable t, String lbl, String val, Font f, Color bg, Color border) {
        PdfPCell c1 = new PdfPCell(new Phrase(lbl, f));
        c1.setBackgroundColor(bg); c1.setPadding(6); c1.setBorderColor(border); c1.setBorderWidth(0.5f); t.addCell(c1);
        PdfPCell c2 = new PdfPCell(new Phrase(val, f));
        c2.setBackgroundColor(bg); c2.setPadding(6); c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorderColor(border); c2.setBorderWidth(0.5f); t.addCell(c2);
    }
}
package WGK.controller;

import WGK.domain.Sneaker;
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

    // ── Ver carrito ──────────────────────────────────────────────────────
    @GetMapping("/ver")
    public String verCarrito(HttpSession session, Model model) {
        List<Sneaker> carrito = getCarrito(session);
        model.addAttribute("carrito", carrito);
        model.addAttribute("total", calcularTotal(carrito));
        return "/carrito/ver";
    }

    // ── Agregar al carrito ───────────────────────────────────────────────
    @PostMapping("/agregar/{idSneaker}")
    public String agregar(@PathVariable Integer idSneaker, HttpSession session) {
        Sneaker sneaker = sneakerService.getSneaker(idSneaker);
        if (sneaker != null) {
            getCarrito(session).add(sneaker);
        }
        return "redirect:/";
    }

    // ── Eliminar un item ─────────────────────────────────────────────────
    @PostMapping("/eliminar/{index}")
    public String eliminar(@PathVariable Integer index, HttpSession session) {
        List<Sneaker> carrito = getCarrito(session);
        if (index >= 0 && index < carrito.size()) {
            carrito.remove((int) index);
        }
        return "redirect:/carrito/ver";
    }

    // ── Vaciar carrito ───────────────────────────────────────────────────
    @PostMapping("/vaciar")
    public String vaciar(HttpSession session) {
        session.removeAttribute("carrito");
        return "redirect:/carrito/ver";
    }

    // ── PAGAR — verifica login y genera PDF ──────────────────────────────
    @PostMapping("/pagar")
    public String pagar(HttpSession session,
                        HttpServletResponse response,
                        RedirectAttributes redirectAttributes) throws Exception {

        // 1. Verificar que el usuario esté autenticado
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean autenticado = auth != null
                && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getPrincipal());

        if (!autenticado) {
            // No está logueado → redirige al login con mensaje de error
            redirectAttributes.addFlashAttribute("errorPago",
                    "Debes iniciar sesión para completar tu compra.");
            return "redirect:/carrito/ver";
        }

        // 2. Obtener carrito
        List<Sneaker> carrito = getCarrito(session);
        if (carrito == null || carrito.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorPago",
                    "Tu carrito está vacío.");
            return "redirect:/carrito/ver";
        }

        BigDecimal total = calcularTotal(carrito);
        String usuario = auth.getName();
        String fechaHora = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"));
        String numFactura = "WGK-" + System.currentTimeMillis();

        // 3. Configurar respuesta HTTP para descarga de PDF
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition",
                "attachment; filename=\"factura_" + numFactura + ".pdf\"");

        // 4. Generar PDF con OpenPDF
        Document doc = new Document(PageSize.A4, 50, 50, 60, 50);
        PdfWriter.getInstance(doc, response.getOutputStream());
        doc.open();

        // ── Fuentes ──
        Font fuenteTitulo    = new Font(Font.HELVETICA, 24, Font.BOLD, Color.BLACK);
        Font fuenteSubtitulo = new Font(Font.HELVETICA, 11, Font.BOLD, Color.DARK_GRAY);
        Font fuenteNormal    = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.DARK_GRAY);
        Font fuenteRojo      = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(220, 0, 0));
        Font fuenteTabHead   = new Font(Font.HELVETICA, 9,  Font.BOLD, Color.WHITE);
        Font fuenteTabCell   = new Font(Font.HELVETICA, 10, Font.NORMAL, Color.BLACK);
        Font fuenteTotal     = new Font(Font.HELVETICA, 13, Font.BOLD, Color.BLACK);
        Font fuentePeq       = new Font(Font.HELVETICA, 8,  Font.NORMAL, Color.GRAY);

        Color colorRojo    = new Color(220, 0, 0);
        Color colorGrisOsc = new Color(40, 40, 40);
        Color colorGrisCla = new Color(245, 245, 245);
        Color colorLinea   = new Color(220, 220, 220);

        // ── Encabezado: logo + título ──
        PdfPTable header = new PdfPTable(2);
        header.setWidthPercentage(100);
        header.setWidths(new float[]{1f, 2f});
        header.setSpacingAfter(20);

        // Celda logo (texto WGK con color rojo)
        Font fuenteLogo = new Font(Font.HELVETICA, 32, Font.BOLD, colorRojo);
        Paragraph logoText = new Paragraph("WGK.", fuenteLogo);
        PdfPCell logoCell = new PdfPCell(logoText);
        logoCell.setBorder(PdfPCell.NO_BORDER);
        logoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        logoCell.setPaddingBottom(8);
        header.addCell(logoCell);

        // Celda info empresa
        Paragraph infoEmpresa = new Paragraph();
        infoEmpresa.add(new Chunk("WegoTKicks PR\n", fuenteSubtitulo));
        infoEmpresa.add(new Chunk("Tienda Certificada de Sneakers\n", fuenteNormal));
        infoEmpresa.add(new Chunk("Puerto Rico · Tel: 3244-2144\n", fuenteNormal));
        infoEmpresa.add(new Chunk("youtube.com/@WeGotKicks", fuenteNormal));
        PdfPCell infoCell = new PdfPCell(infoEmpresa);
        infoCell.setBorder(PdfPCell.NO_BORDER);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        header.addCell(infoCell);

        doc.add(header);

        // ── Línea divisora roja ──
        PdfPTable lineaRoja = new PdfPTable(1);
        lineaRoja.setWidthPercentage(100);
        lineaRoja.setSpacingAfter(16);
        PdfPCell celdaLinea = new PdfPCell(new Phrase(" "));
        celdaLinea.setBackgroundColor(colorRojo);
        celdaLinea.setBorder(PdfPCell.NO_BORDER);
        celdaLinea.setFixedHeight(3f);
        lineaRoja.addCell(celdaLinea);
        doc.add(lineaRoja);

        // ── Datos de la factura ──
        Font fuenteFactTitulo = new Font(Font.HELVETICA, 18, Font.BOLD, colorGrisOsc);
        doc.add(new Paragraph("FACTURA DE COMPRA", fuenteFactTitulo));
        doc.add(new Paragraph(" "));

        PdfPTable datosFact = new PdfPTable(2);
        datosFact.setWidthPercentage(100);
        datosFact.setSpacingAfter(20);

        // Columna izquierda: cliente
        Paragraph colCliente = new Paragraph();
        colCliente.add(new Chunk("FACTURADO A\n", fuenteRojo));
        colCliente.add(new Chunk("Cliente: " + usuario + "\n", fuenteNormal));
        colCliente.add(new Chunk("Verificación: 100% Legit\n", fuenteNormal));
        colCliente.add(new Chunk("Envío: Gratis", fuenteNormal));
        PdfPCell cellCliente = new PdfPCell(colCliente);
        cellCliente.setBorder(PdfPCell.NO_BORDER);
        datosFact.addCell(cellCliente);

        // Columna derecha: número y fecha
        Paragraph colFactura = new Paragraph();
        colFactura.add(new Chunk("DETALLES\n", fuenteRojo));
        colFactura.add(new Chunk("Nº Factura: " + numFactura + "\n", fuenteNormal));
        colFactura.add(new Chunk("Fecha: " + fechaHora + "\n", fuenteNormal));
        colFactura.add(new Chunk("Estado: Pagado", fuenteNormal));
        PdfPCell cellFactura = new PdfPCell(colFactura);
        cellFactura.setBorder(PdfPCell.NO_BORDER);
        cellFactura.setHorizontalAlignment(Element.ALIGN_RIGHT);
        datosFact.addCell(cellFactura);

        doc.add(datosFact);

        // ── Tabla de productos ──
        PdfPTable tabla = new PdfPTable(4);
        tabla.setWidthPercentage(100);
        tabla.setWidths(new float[]{3f, 1.5f, 1f, 1.2f});
        tabla.setSpacingAfter(20);

        // Cabecera de tabla
        String[] cabeceras = {"PRODUCTO", "MARCA", "CANT.", "PRECIO"};
        for (String cab : cabeceras) {
            PdfPCell cell = new PdfPCell(new Phrase(cab, fuenteTabHead));
            cell.setBackgroundColor(colorGrisOsc);
            cell.setPadding(8);
            cell.setBorder(PdfPCell.NO_BORDER);
            tabla.addCell(cell);
        }

        // Filas de productos
        boolean fila = false;
        for (Sneaker s : carrito) {
            Color bgFila = fila ? colorGrisCla : Color.WHITE;

            PdfPCell cNombre = new PdfPCell(new Phrase(s.getDescripcion(), fuenteTabCell));
            cNombre.setBackgroundColor(bgFila);
            cNombre.setPadding(8);
            cNombre.setBorderColor(colorLinea);
            cNombre.setBorderWidth(0.5f);
            tabla.addCell(cNombre);

            String marca = s.getMarca() != null ? s.getMarca().getDescripcion() : "—";
            PdfPCell cMarca = new PdfPCell(new Phrase(marca, fuenteTabCell));
            cMarca.setBackgroundColor(bgFila);
            cMarca.setPadding(8);
            cMarca.setBorderColor(colorLinea);
            cMarca.setBorderWidth(0.5f);
            tabla.addCell(cMarca);

            PdfPCell cCant = new PdfPCell(new Phrase("1", fuenteTabCell));
            cCant.setBackgroundColor(bgFila);
            cCant.setPadding(8);
            cCant.setHorizontalAlignment(Element.ALIGN_CENTER);
            cCant.setBorderColor(colorLinea);
            cCant.setBorderWidth(0.5f);
            tabla.addCell(cCant);

            String precio = "$" + s.getPrecio().toString();
            PdfPCell cPrecio = new PdfPCell(new Phrase(precio, fuenteTabCell));
            cPrecio.setBackgroundColor(bgFila);
            cPrecio.setPadding(8);
            cPrecio.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cPrecio.setBorderColor(colorLinea);
            cPrecio.setBorderWidth(0.5f);
            tabla.addCell(cPrecio);

            fila = !fila;
        }
        doc.add(tabla);

        // ── Totales ──
        PdfPTable totales = new PdfPTable(2);
        totales.setWidthPercentage(45);
        totales.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totales.setSpacingAfter(30);

        // Subtotal
        addFilaTotales(totales, "Subtotal:", "$" + total, fuenteNormal, Color.WHITE);
        addFilaTotales(totales, "Envío:", "Gratis", fuenteNormal, Color.WHITE);
        addFilaTotales(totales, "Verificación Legit:", "Incluido", fuenteNormal, Color.WHITE);

        // Línea
        PdfPCell lineaSep = new PdfPCell(new Phrase(" "));
        lineaSep.setColspan(2);
        lineaSep.setBackgroundColor(colorLinea);
        lineaSep.setFixedHeight(1f);
        lineaSep.setBorder(PdfPCell.NO_BORDER);
        totales.addCell(lineaSep);

        // Total final con fondo rojo
        PdfPCell labelTotal = new PdfPCell(new Phrase("TOTAL:", fuenteTabHead));
        labelTotal.setBackgroundColor(colorRojo);
        labelTotal.setPadding(8);
        labelTotal.setBorder(PdfPCell.NO_BORDER);
        totales.addCell(labelTotal);

        PdfPCell valorTotal = new PdfPCell(new Phrase("$" + total, fuenteTabHead));
        valorTotal.setBackgroundColor(colorRojo);
        valorTotal.setPadding(8);
        valorTotal.setHorizontalAlignment(Element.ALIGN_RIGHT);
        valorTotal.setBorder(PdfPCell.NO_BORDER);
        totales.addCell(valorTotal);

        doc.add(totales);

        // ── Pie de página ──
        PdfPTable pie = new PdfPTable(1);
        pie.setWidthPercentage(100);

        PdfPCell celdaPie = new PdfPCell();
        celdaPie.setBorderColor(colorLinea);
        celdaPie.setBorderWidth(0.5f);
        celdaPie.setBorder(PdfPCell.TOP);
        celdaPie.setPaddingTop(10);

        Paragraph textoPie = new Paragraph();
        textoPie.add(new Chunk("¡Gracias por tu compra en WegoTKicks PR!\n", fuenteSubtitulo));
        textoPie.add(new Chunk(
                "Todos nuestros sneakers son 100% verificados y certificados antes del envío.\n",
                fuentePeq));
        textoPie.add(new Chunk(
                "Contacto: 3244-2144 · youtube.com/@WeGotKicks · Puerto Rico",
                fuentePeq));
        textoPie.setAlignment(Element.ALIGN_CENTER);
        celdaPie.addElement(textoPie);
        pie.addCell(celdaPie);
        doc.add(pie);

        doc.close();

        // 5. Vaciar el carrito después del pago
        session.removeAttribute("carrito");

        return null; // La respuesta ya fue enviada como PDF
    }

    // ── Helpers ──────────────────────────────────────────────────────────
    @SuppressWarnings("unchecked")
    private List<Sneaker> getCarrito(HttpSession session) {
        List<Sneaker> carrito = (List<Sneaker>) session.getAttribute("carrito");
        if (carrito == null) {
            carrito = new ArrayList<>();
            session.setAttribute("carrito", carrito);
        }
        return carrito;
    }

    private BigDecimal calcularTotal(List<Sneaker> carrito) {
        return carrito.stream()
                .map(Sneaker::getPrecio)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private void addFilaTotales(PdfPTable table, String label, String valor,
                                Font fuente, Color bg) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, fuente));
        c1.setBackgroundColor(bg);
        c1.setPadding(6);
        c1.setBorderColor(new Color(220, 220, 220));
        c1.setBorderWidth(0.5f);
        table.addCell(c1);

        PdfPCell c2 = new PdfPCell(new Phrase(valor, fuente));
        c2.setBackgroundColor(bg);
        c2.setPadding(6);
        c2.setHorizontalAlignment(Element.ALIGN_RIGHT);
        c2.setBorderColor(new Color(220, 220, 220));
        c2.setBorderWidth(0.5f);
        table.addCell(c2);
    }
}
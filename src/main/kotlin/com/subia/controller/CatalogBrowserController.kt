package com.subia.controller

import com.subia.service.CatalogService
import com.subia.service.CategoryService
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping

/**
 * Controlador MVC para el buscador/directorio de aplicaciones del catálogo.
 *
 * Sirve la página /catalog-browser que permite explorar todos los servicios conocidos,
 * filtrarlos por categoría y añadirlos directamente como suscripciones activas.
 */
@Controller
@RequestMapping("/catalog-browser")
class CatalogBrowserController(
    private val catalogService: CatalogService,
    private val categoryService: CategoryService
) {

    @GetMapping
    fun browse(model: Model): String {
        model.addAttribute("allItems", catalogService.getAllItems())

        // Construye un mapa categoryKey → ID de la entidad JPA de Categoría.
        // Permite que el template genere el form POST correcto para cada card del catálogo.
        val categoryKeyToId = categoryService.findAll()
            .mapNotNull { cat ->
                val key = when (cat.name) {
                    "IA"                 -> "ia"
                    "Streaming"          -> "streaming"
                    "Música"             -> "musica"
                    "Software"           -> "software"
                    "Cloud"              -> "cloud"
                    "Gaming"             -> "gaming"
                    "Seguridad"          -> "seguridad"
                    "Noticias y Lectura" -> "noticias"
                    "Salud y Deporte"    -> "salud"
                    "Desarrollo"         -> "desarrollo"
                    "Prueba gratuita"    -> "prueba"
                    "Finanzas"           -> "finanzas"
                    "Educación"          -> "educacion"
                    "Creatividad y foto" -> "creatividad"
                    "Citas y social"     -> "citas"
                    else                 -> null
                }
                if (key != null) key to cat.id else null
            }.toMap()
        model.addAttribute("categoryKeyToId", categoryKeyToId)
        return "catalog-browser"
    }
}

package ir.tolooesalamat.app.dto

import org.springframework.data.domain.Page
import org.springframework.data.domain.PageRequest
import org.springframework.data.domain.Sort

data class PagedResponse<T>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val hasNext: Boolean = !last,
    val hasPrevious: Boolean = !first
) {
    companion object {
        fun <T : Any> from(page: Page<T>): PagedResponse<T> = PagedResponse(
            content = page.content,
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            first = page.isFirst,
            last = page.isLast,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )

        fun <E : Any, D> from(page: Page<E>, mapper: (E) -> D): PagedResponse<D> = PagedResponse(
            content = page.content.map(mapper),
            page = page.number,
            size = page.size,
            totalElements = page.totalElements,
            totalPages = page.totalPages,
            first = page.isFirst,
            last = page.isLast,
            hasNext = page.hasNext(),
            hasPrevious = page.hasPrevious()
        )

        fun <T> empty(page: Int = 0, size: Int = 20): PagedResponse<T> = PagedResponse(
            content = emptyList(),
            page = page,
            size = size,
            totalElements = 0,
            totalPages = 0,
            first = true,
            last = true,
            hasNext = false,
            hasPrevious = false
        )
    }
}

data class PageRequestDto(
    val page: Int = 0,
    val size: Int = 20,
    val sortBy: String = "createdAt",
    val sortDirection: String = "DESC"
) {
    fun toPageable(): PageRequest {
        val direction = if (sortDirection.equals("ASC", ignoreCase = true))
            Sort.Direction.ASC
        else
            Sort.Direction.DESC

        return PageRequest.of(
            page.coerceAtLeast(0),
            size.coerceIn(1, 100),
            Sort.by(direction, sortBy)
        )
    }
}
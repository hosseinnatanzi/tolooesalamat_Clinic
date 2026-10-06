package ir.tolooesalamat.app.dto

import org.springframework.data.domain.Page

data class PagedResponse<T : Any>(
    val content: List<T>,
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val first: Boolean,
    val last: Boolean,
    val hasNext: Boolean,
    val hasPrevious: Boolean
) {
    companion object {

        /**
         * تبدیل Page<T> به PagedResponse<T>.
         */
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

        /**
         * تبدیل Page<E> به PagedResponse<D> با mapper.
         */
        fun <E : Any, D : Any> from(
            page: Page<E>,
            mapper: (E) -> D
        ): PagedResponse<D> = PagedResponse(
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

        /**
         * ساخت PagedResponse خالی.
         */
        fun <T : Any> empty(page: Int = 0, size: Int = 20): PagedResponse<T> =
            PagedResponse(
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
package co.adityarajput.notifilter.data.filter

class FiltersRepository(private val filterDao: FilterDao) {
    suspend fun create(filter: Filter) = filterDao.create(filter)

    suspend fun replaceAll(filters: List<Filter>) {
        filterDao.deleteAll()
        filterDao.createAll(filters)
    }

    fun list() = filterDao.list()

    suspend fun registerHit(filter: Filter) = filterDao.registerHit(filter.id)

    suspend fun toggleHistory(filter: Filter) = filterDao.toggleHistory(filter.id)

    suspend fun toggleEnabled(filter: Filter) = filterDao.toggleEnabled(filter.id)

    suspend fun delete(filter: Filter) = filterDao.delete(filter)
}

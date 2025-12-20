package co.adityarajput.notifilter.data.filter

class FiltersRepository(private val filterDao: FilterDao) {
    suspend fun create(filter: Filter) = filterDao.create(filter)

    fun list() = filterDao.list()

    suspend fun registerHit(filter: Filter) = filterDao.registerHit(filter.id)

    suspend fun toggleEnabled(filter: Filter) = filterDao.toggleEnabled(filter.id)

    suspend fun delete(filter: Filter) = filterDao.delete(filter)
}

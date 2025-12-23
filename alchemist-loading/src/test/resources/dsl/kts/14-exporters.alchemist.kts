import it.unibo.alchemist.boundary.dsl.Dsl.incarnation
import it.unibo.alchemist.boundary.dsl.Dsl.simulation

val incarnation = PROTELIS.incarnation<Any, Euclidean2DPosition>()
simulation(incarnation) {
    exporter {
        type = CSVExporter(
            "test_export_interval",
            4.0,
        )
        data(
            Time(),
            moleculeReader(
                "default_module:default_program",
                null,
                CommonFilters.NOFILTER.filteringPolicy,
                emptyList(),
            ),
        )
    }
}

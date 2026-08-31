package nexusmarket.domain.valueobjects;

/**
 * DomainCatalog (abstract concept).
 *
 * <p>Represents a generic business catalog used throughout the NexusMarket
 * domain. It provides a consistent structure for controlled business values
 * that require a {@code code}, a human-readable {@code name} and a business
 * {@code description}.</p>
 *
 * <p>Characteristics defined by the Domain Value Objects document:</p>
 * <ul>
 *   <li>Immutable.</li>
 *   <li>Equality is determined by value rather than object identity.</li>
 *   <li>Catalog values are controlled by the domain and must not be
 *       represented by arbitrary strings throughout the application.</li>
 *   <li>Each catalog value must have a unique {@code code}.</li>
 * </ul>
 *
 * <p>All business catalogs inherit from {@code DomainCatalog}. Because Java
 * enums cannot extend a class, the catalogs are modeled as enums that
 * implement this interface, which preserves the inheritance contract while
 * guaranteeing a fixed set of allowed values.</p>
 */
public interface DomainCatalog {

    /** Unique business identifier of the catalog value. */
    String getCode();

    /** Human-readable name displayed within the application. */
    String getName();

    /** Business definition of the catalog value. */
    String getDescription();
}

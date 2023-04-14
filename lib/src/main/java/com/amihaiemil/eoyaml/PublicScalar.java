package com.amihaiemil.eoyaml;

public class PublicScalar extends BaseScalar{


    private final Comment comment;

    /**
     * This scalar's value.
     */
    private final String value;

    /**
     * Ctor.
     * @param value Given value for this scalar.
     */
    public PublicScalar(final String value) {
        this(value, "");
    }

    /**
     * Ctor.
     * @param value Given value for this scalar.
     * @param inline Comment inline with the scalar (after it).
     */
    public PublicScalar(final String value, final String inline) {
        this(value, "", inline);
    }

    /**
     * Ctor.
     * @param value Given value for this scalar.
     * @param above Comment above the scalar.
     * @param inline Comment inline with the scalar.
     */
    PublicScalar(
            final String value, final String above, final String inline
    ) {
        this.value = value;
        this.comment = new Concatenated(
                new BuiltComment(
                        this, above
                ),
                new InlineComment(
                        new BuiltComment(this, inline)
                )
        );
    }

    /**
     * Value of this scalar.
     * @return Value of type T.
     */
    @Override
    public String value() {
        return this.value;
    }

    @Override
    public Comment comment() {
        return this.comment;
    }

}

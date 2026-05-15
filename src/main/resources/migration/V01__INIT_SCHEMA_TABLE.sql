CREATE SCHEMA IF NOT EXISTS splitbill;


CREATE TABLE splitbill.billing (
                                   id          BIGSERIAL       NOT NULL,
                                   name        VARCHAR(100)    NOT NULL,
                                   created_at  TIMESTAMP       NULL,
                                   updated_at  TIMESTAMP       NULL,

                                   CONSTRAINT pk_billing PRIMARY KEY (id)
);

CREATE TABLE splitbill.participants (
                                        id          BIGSERIAL       NOT NULL,
                                        name        VARCHAR(255)    NOT NULL,
                                        billing_id  BIGINT          NOT NULL,
                                        created_at  TIMESTAMP       NULL,
                                        updated_at  TIMESTAMP       NULL,

                                        CONSTRAINT pk_participants      PRIMARY KEY (id),
                                        CONSTRAINT fk_participants_billing
                                            FOREIGN KEY (billing_id)
                                                REFERENCES splitbill.billing (id)
                                                ON DELETE CASCADE,
                                        CONSTRAINT uq_participants_billing_name
                                            UNIQUE (billing_id, name)
);

CREATE TABLE splitbill.expense (
                                   id              BIGSERIAL           NOT NULL,
                                   description     VARCHAR(255)        NOT NULL,
                                   amount          NUMERIC(19, 4)      NOT NULL,
                                   expense_type    VARCHAR(50)         NOT NULL,
                                   split_strategy  VARCHAR(50)         NOT NULL,
                                   paid_by_id      BIGINT              NOT NULL,
                                   billing_id      BIGINT              NOT NULL,
                                   created_at      TIMESTAMP           NULL,
                                   updated_at      TIMESTAMP           NULL,

                                   CONSTRAINT pk_expense PRIMARY KEY (id),
                                   CONSTRAINT fk_expense_paid_by
                                       FOREIGN KEY (paid_by_id)
                                           REFERENCES splitbill.participants (id),
                                   CONSTRAINT fk_expense_billing
                                       FOREIGN KEY (billing_id)
                                           REFERENCES splitbill.billing (id)
                                           ON DELETE CASCADE,
                                   CONSTRAINT chk_expense_amount
                                       CHECK (amount > 0)
);

CREATE TABLE splitbill.expense_share (
                                         id                  BIGSERIAL       NOT NULL,
                                         expense_id          BIGINT          NOT NULL,
                                         participant_id      BIGINT          NOT NULL,
                                         amount              NUMERIC(19, 4)  NOT NULL,
                                         percentage_share    NUMERIC(5, 2)   NULL,
                                         created_at          TIMESTAMP       NULL,
                                         updated_at          TIMESTAMP       NULL,

                                         CONSTRAINT pk_expense_share PRIMARY KEY (id),
                                         CONSTRAINT fk_expense_share_expense
                                             FOREIGN KEY (expense_id)
                                                 REFERENCES splitbill.expense (id)
                                                 ON DELETE CASCADE,
                                         CONSTRAINT fk_expense_share_participant
                                             FOREIGN KEY (participant_id)
                                                 REFERENCES splitbill.participants (id),
                                         CONSTRAINT chk_expense_share_amount
                                             CHECK (amount > 0),
                                         CONSTRAINT chk_percentage_share_range
                                             CHECK (percentage_share IS NULL OR (percentage_share > 0 AND percentage_share <= 100))
);

CREATE TABLE splitbill.payments (
                                    id              BIGSERIAL       NOT NULL,
                                    payer_id        BIGINT          NOT NULL,
                                    payee_id        BIGINT          NOT NULL,
                                    amount          NUMERIC(19, 4)  NOT NULL,
                                    billing_id      BIGINT          NOT NULL,
                                    note            VARCHAR(500)    NULL,
                                    paid_at         TIMESTAMP       NOT NULL,
                                    created_at      TIMESTAMP       NULL,
                                    updated_at      TIMESTAMP       NULL,

                                    CONSTRAINT pk_payments PRIMARY KEY (id),
                                    CONSTRAINT fk_payments_payer
                                        FOREIGN KEY (payer_id)
                                            REFERENCES splitbill.participants (id),
                                    CONSTRAINT fk_payments_payee
                                        FOREIGN KEY (payee_id)
                                            REFERENCES splitbill.participants (id),
                                    CONSTRAINT fk_payments_billing
                                        FOREIGN KEY (billing_id)
                                            REFERENCES splitbill.billing (id)
                                            ON DELETE CASCADE,
                                    CONSTRAINT chk_payments_amount
                                        CHECK (amount > 0)
);

CREATE TABLE splitbill.settlements (
                                       id                      BIGSERIAL       NOT NULL,
                                       billing_id              BIGINT          NOT NULL,
                                       total_expenses          NUMERIC(19, 4)  NOT NULL,
                                       grand_total             NUMERIC(19, 4)  NOT NULL,
                                       created_at              TIMESTAMP       NOT NULL,
                                       updated_at              TIMESTAMP       NULL,

                                       CONSTRAINT pk_settlements PRIMARY KEY (id),
                                       CONSTRAINT fk_settlements_billing
                                           FOREIGN KEY (billing_id)
                                               REFERENCES splitbill.billing (id)
                                               ON DELETE CASCADE,
                                       CONSTRAINT chk_settlements_total_expenses
                                           CHECK (total_expenses >= 0),
                                       CONSTRAINT chk_settlements_grand_total
                                           CHECK (grand_total >= 0)
);

CREATE TABLE splitbill.settlement_transactions (
                                                   id                      BIGSERIAL       NOT NULL,
                                                   settlement_id           BIGINT          NOT NULL,
                                                   from_participant_id     BIGINT          NOT NULL,
                                                   to_participant_id       BIGINT          NOT NULL,
                                                   amount                  NUMERIC(19, 4)  NOT NULL,
                                                   created_at              TIMESTAMP       NULL,
                                                   updated_at              TIMESTAMP       NULL,

                                                   CONSTRAINT pk_settlement_transactions PRIMARY KEY (id),
                                                   CONSTRAINT fk_settlement_transactions_settlement
                                                       FOREIGN KEY (settlement_id)
                                                           REFERENCES splitbill.settlements (id)
                                                           ON DELETE CASCADE,
                                                   CONSTRAINT fk_settlement_transactions_from
                                                       FOREIGN KEY (from_participant_id)
                                                           REFERENCES splitbill.participants (id),
                                                   CONSTRAINT fk_settlement_transactions_to
                                                       FOREIGN KEY (to_participant_id)
                                                           REFERENCES splitbill.participants (id),
                                                   CONSTRAINT chk_settlement_transactions_amount
                                                       CHECK (amount > 0)
);


CREATE INDEX idx_billing_name
    ON splitbill.billing (name);

CREATE INDEX idx_participants_billing_id
    ON splitbill.participants (billing_id);

CREATE INDEX idx_expense_billing_id
    ON splitbill.expense (billing_id);

CREATE INDEX idx_expense_paid_by_id
    ON splitbill.expense (paid_by_id);

CREATE INDEX idx_expense_billing_type
    ON splitbill.expense (billing_id, expense_type);

CREATE INDEX idx_expense_created_at
    ON splitbill.expense (billing_id, created_at);

CREATE INDEX idx_expense_share_expense_id
    ON splitbill.expense_share (expense_id);

CREATE INDEX idx_expense_share_participant_id
    ON splitbill.expense_share (participant_id);

CREATE INDEX idx_expense_share_expense_participant
    ON splitbill.expense_share (expense_id, participant_id);

CREATE INDEX idx_payments_billing_id
    ON splitbill.payments (billing_id);

CREATE INDEX idx_payments_payer_id
    ON splitbill.payments (payer_id);

CREATE INDEX idx_payments_payee_id
    ON splitbill.payments (payee_id);

CREATE INDEX idx_payments_paid_at
    ON splitbill.payments (billing_id, paid_at);

CREATE INDEX idx_settlements_billing_id
    ON splitbill.settlements (billing_id, created_at DESC);

CREATE INDEX idx_settlement_transactions_settlement_id
    ON splitbill.settlement_transactions (settlement_id);

CREATE INDEX idx_settlement_transactions_from
    ON splitbill.settlement_transactions (from_participant_id);

CREATE INDEX idx_settlement_transactions_to
    ON splitbill.settlement_transactions (to_participant_id);
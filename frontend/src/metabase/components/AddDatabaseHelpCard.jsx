import React, { useMemo } from "react";
import PropTypes from "prop-types";
import { Flex } from "grid-styled";

import { t, jt } from "ttag";

import MetabaseSettings from "metabase/lib/settings";

import ExternalLink from "metabase/components/ExternalLink";
import Icon from "metabase/components/Icon";

const propTypes = {
  engine: PropTypes.string,
};

export const ENGINE_DOCS = {
  bigquery: MetabaseSettings.docsUrl("administration-guide/databases/bigquery"),
  mongo: MetabaseSettings.docsUrl("administration-guide/databases/mongodb"),
  mysql: MetabaseSettings.docsUrl("administration-guide/databases/mysql"),
  oracle: MetabaseSettings.docsUrl("administration-guide/databases/oracle"),
  snowflake: MetabaseSettings.docsUrl(
    "administration-guide/databases/snowflake",
  ),
  vertica: MetabaseSettings.docsUrl("administration-guide/databases/vertica"),
};

export const GENERAL_DB_DOC = MetabaseSettings.docsUrl(
  "administration-guide/01-managing-databases",
);

export const CLOUD_HELP_URL = "https://www.metabase.com/help/cloud";

function AddDatabaseHelpCard({ engine, ...props }) {
  const displayName = useMemo(() => {
    const hasEngineDoc = !!ENGINE_DOCS[engine];
    if (!hasEngineDoc) {
      return "your database";
    }
    const engines = MetabaseSettings.get("engines");
    return (engines[engine] || {})["driver-name"];
  }, [engine]);

  const docsLink = ENGINE_DOCS[engine] || GENERAL_DB_DOC;
  const shouldDisplayHelpLink = MetabaseSettings.isHosted();

  return (
    <Flex
      p={2}
      style={{ backgroundColor: "#F9FBFB", borderRadius: 10, minWidth: 300 }}
      {...props}
    >
      <Flex
        align="center"
        justify="center"
        className="flex-no-shrink circular"
        style={{
          width: 52,
          height: 52,
          backgroundColor: "#EEF2F5",
        }}
      >
        <Icon size={20} name="database" className="text-brand" />
      </Flex>
      <Flex
        flexDirection="column"
        justify="center"
        className="ml2"
        style={{ marginTop: shouldDisplayHelpLink ? "8px" : 0 }}
      >
        <div>
          <p className="text-medium m0">
            {t`Need help setting up`} {displayName}?
          </p>
          <ExternalLink href={docsLink} className="text-brand text-bold">
            {t`Our docs can help.`}
          </ExternalLink>
        </div>
        {shouldDisplayHelpLink && (
          <p className="mt2 text-medium m0">
            {jt`Docs weren't enough?`}{" "}
            <ExternalLink
              href={CLOUD_HELP_URL}
              className="text-brand text-bold"
            >
              Write us.
            </ExternalLink>
          </p>
        )}
      </Flex>
    </Flex>
  );
}

AddDatabaseHelpCard.propTypes = propTypes;

export default AddDatabaseHelpCard;

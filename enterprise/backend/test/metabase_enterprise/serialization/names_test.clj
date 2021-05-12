(ns metabase-enterprise.serialization.names-test
  (:require [clojure.test :refer :all]
            [metabase-enterprise.serialization.names :as names]
            [metabase-enterprise.serialization.test-util :as ts]
            [metabase.models :refer [Card Collection Dashboard Database Field Metric NativeQuerySnippet Segment Table]]
            [metabase.util :as u]
            [metabase.test :as mt]))

(deftest safe-name-test
  (are [s expected] (= (names/safe-name {:name s}) expected)
    "foo"         "foo"
    "foo/bar baz" "foo%2Fbar baz"))

(deftest unescape-name-test
  (are [s expected] (= expected
                       (names/unescape-name s))
    "foo"           "foo"
    "foo%2Fbar baz" "foo/bar baz"))

(deftest safe-name-unescape-name-test
 (is (= "foo/bar baz"
        (-> {:name "foo/bar baz"} names/safe-name names/unescape-name))))

(deftest roundtrip-test
  (ts/with-world
    (doseq [object [(Card card-id-root)
                    (Card card-id)
                    (Card card-id-nested)
                    (Table table-id)
                    (Field category-field-id)
                    (Metric metric-id)
                    (Segment segment-id)
                    (Collection collection-id)
                    (Collection collection-id-nested)
                    (Dashboard dashboard-id)
                    (Database db-id)
                    (NativeQuerySnippet snippet-id)]]
      (testing (class object)
        (let [context (names/fully-qualified-name->context (names/fully-qualified-name object))
              id-fn   (some-fn :snippet :field :metric :segment :card :dashboard :collection :table :database)]
          (is (= (u/the-id object)
                 (id-fn context))))))))

(deftest fully-qualified-name->context-test
  (testing "fully-qualified-name->context works as expected"
    (testing "with cards in root and in a collection"
      (mt/with-temp* [Collection [{collection-id :id} {:name "A Collection"}]
                      Card       [root-card {:name "Root Card"}]
                      Card       [collection-card {:name         "Collection Card"
                                                   :collection_id collection-id}]]
        (let [card1-name "/collections/root/cards/Root Card"
              card2-name "/collections/root/collections/A Collection/cards/Collection Card"]
          (is (= card1-name (names/fully-qualified-name root-card)))
          (is (= card2-name (names/fully-qualified-name collection-card))))))
    (testing "with snippets in a collection"
      (mt/with-temp* [Collection [{base-collection-id :id} {:name "Base Collection"
                                                            :namespace "snippets"}]
                      Collection [{collection-id :id}      {:name "Nested Collection"
                                                            :location (format "/%s/" base-collection-id)
                                                            :namespace "snippets"}]
                      NativeQuerySnippet [snippet {:content "price > 2"
                                                   :name "Price > 2"
                                                   :description "Price more than 2"
                                                   :collection_id collection-id}]]
         (let [fully-qualified-name (str "/collections/root/collections/:snippets/Base Collection/collections"
                                         "/:snippets/Nested Collection/snippets/Price %3E 2")]
           (is (= fully-qualified-name
                  (names/fully-qualified-name snippet)))
           (is (= {:collection collection-id
                   :snippet    (u/the-id snippet)}
                  (names/fully-qualified-name->context fully-qualified-name))))))))

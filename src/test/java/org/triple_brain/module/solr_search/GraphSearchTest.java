package org.triple_brain.module.solr_search;

import org.junit.Ignore;
import org.junit.Test;
import org.triple_brain.module.model.graph.GraphElement;
import org.triple_brain.module.model.graph.edge.Edge;
import org.triple_brain.module.search.EdgeSearchResult;
import org.triple_brain.module.search.GraphElementSearchResult;
import org.triple_brain.module.search.VertexSearchResult;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.*;

/*
* Copyright Mozilla Public License 1.1
*/
public class GraphSearchTest extends SearchRelatedTest {

    @Test
    public void can_search_vertices_for_auto_completion() throws Exception {
        indexGraph();
        indexVertex(pineApple);
        List<VertexSearchResult> vertices;
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel("vert", user);
        assertThat(vertices.size(), is(3));
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel("vertex Cad", user);
        assertThat(vertices.size(), is(1));
        GraphElement firstVertex = vertices.get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(firstVertex.label(), is("vertex Cadeau"));
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel("pine A", user);
        assertThat(vertices.size(), is(1));
    }

    @Test
    public void cant_search_in_vertices_of_another_user() throws Exception {
        indexGraph();
        indexVertex(pineApple);
        List<VertexSearchResult> vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "vert",
                user
        );
        assertTrue(vertices.size() > 0);
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertFalse(vertices.size() > 0);
    }

    @Test
    public void vertex_note_can_be_retrieved_from_search() throws Exception {
        vertexA.comment("A description");
        indexGraph();
        List<VertexSearchResult> searchResults = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                vertexA.label(),
                user
        );
        GraphElement vertex = searchResults.get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.comment(),
                is("A description")
        );
    }

    @Test
    public void vertex_relations_name_can_be_retrieved() throws Exception {
        indexGraph();
        List<VertexSearchResult> searchResults = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                vertexA.label(),
                user
        );
        VertexSearchResult result = searchResults.get(0);
        assertTrue(
                result.getRelationsName().contains(
                        "between vertex A and vertex B"
                ));
    }

    @Test
    public void incoming_and_outgoing_vertex_relations_name_can_be_retrieved() throws Exception {
        indexGraph();
        List<VertexSearchResult> searchResults = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                vertexB.label(),
                user
        );
        VertexSearchResult result = searchResults.get(0);
        assertTrue(
                result.getRelationsName().contains(
                        "between vertex A and vertex B"
                )
        );
        assertTrue(
                result.getRelationsName().contains(
                "between vertex B and vertex C"
        ));
    }

    @Test
    public void can_search_for_other_users_public_vertices() {
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertFalse(vertices.size() > 0);
        vertexA.makePublic();
        indexVertex(vertexA);
        vertices = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertTrue(vertices.size() > 0);
    }

    @Test
    public void searching_for_own_vertices_only_does_not_return_vertices_of_other_users() {
        vertexA.makePublic();
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertTrue(vertices.size() > 0);
        vertices = graphSearch.searchOnlyForOwnVerticesForAutoCompletionByLabel(
                "vert",
                user2
        );
        assertFalse(vertices.size() > 0);
    }

    @Test
    public void search_is_case_insensitive() {
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vert",
                user
        );
        assertTrue(vertices.size() > 0);
        vertices = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "Vert",
                user
        );
        assertTrue(vertices.size() > 0);
    }

    @Test
    public void case_is_preserved_when_getting_label() {
        vertexA.label("Vertex Azure");
        indexGraph();
        List<VertexSearchResult> vertices = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "vertex azure",
                user
        );
        GraphElement vertex = vertices.get(0).getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.label(),
                is("Vertex Azure")
        );
    }

    @Test
    public void relation_source_and_destination_vertex_uri_are_included_in_result() {
        indexGraph();
        List<EdgeSearchResult> relations = graphSearch.searchRelationsForAutoCompletionByLabel(
                "between vert",
                user
        );
        Edge edge = relations.get(0).getEdge();
        assertFalse(
                null == edge.sourceVertex().uri()
        );
        assertFalse(
                null == edge.destinationVertex().uri()
        );
    }

    @Test
    @Ignore("I dont know why but this test fails sometimes and succeeds in other times")
    public void can_search_relations() {
        indexGraph();
        List<EdgeSearchResult> results = graphSearch.searchRelationsForAutoCompletionByLabel(
                "between vert",
                user
        );
        assertThat(results.size(), is(2));
    }

    @Test
    public void can_search_by_uri() {
        indexGraph();
        GraphElementSearchResult searchResult = graphSearch.getByUri(
                vertexA.uri(),
                user
        );
        GraphElement vertex = searchResult.getGraphElementSearchResult().getGraphElement();
        assertThat(
                vertex.label(),
                is(vertexA.label())
        );
    }

    @Test
    @Ignore
    //todo
    public void search_goes_beyond_two_first_words() {
        vertexA.label(
                "bonjour monsieur proute"
        );
        vertexB.label(
                "bonjour monsieur pratte"
        );
        vertexC.label(
                "bonjour monsieur avion"
        );
        indexGraph();

        List<VertexSearchResult> vertices = graphSearch.searchOwnVerticesAndPublicOnesForAutoCompletionByLabel(
                "bonjour monsieur pr",
                user
        );
        assertThat(vertices.size(), is(2));
    }
}
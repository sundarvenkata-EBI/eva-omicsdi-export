/*
 * Copyright 2017 EMBL - European Bioinformatics Institute
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package uk.ac.ebi.eva.bd2k.transform;

import org.junit.Before;
import org.junit.Test;
import uk.ac.ebi.ddi.xml.validator.parser.model.AdditionalFields;
import uk.ac.ebi.ddi.xml.validator.parser.model.Database;
import uk.ac.ebi.ddi.xml.validator.parser.model.Entry;
import uk.ac.ebi.ddi.xml.validator.parser.model.Field;

import uk.ac.ebi.eva.bd2k.client.ProjectClient;
import uk.ac.ebi.eva.bd2k.export.EvaStudyTransformer;
import uk.ac.ebi.eva.bd2k.model.EnaProject;
import uk.ac.ebi.eva.bd2k.model.VariantStudy;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static uk.ac.ebi.eva.bd2k.export.EvaStudyTransformer.EVA_FIRST_PUBLISHED_DATE;
import static uk.ac.ebi.eva.bd2k.export.EvaStudyTransformer.FULL_DATASET_LINK;
import static uk.ac.ebi.eva.bd2k.export.EvaStudyTransformer.INSTRUMENT_PLATFORM;
import static uk.ac.ebi.eva.bd2k.export.EvaStudyTransformer.PUBLICATION_DATE;
import static uk.ac.ebi.eva.bd2k.export.EvaStudyTransformer.SPECIES;
import static uk.ac.ebi.eva.bd2k.export.EvaStudyTransformer.TECHNOLOGY_TYPE;

public class StudyTransformerTest {

    public static final String EVA_STUDY_PUBLICATION_DATE = "2017-01-01";

    public static final String PRE_EVA_STUDY_PUBLICATION_DATE = "2014-01-01";

    private ProjectClient projectClientMock;

    private ProjectClient preEvaProjectClientMock;

    private VariantStudy variantStudy;

    private String studyId;

    private String studyName;

    private String studyDescription;

    private String center;

    private String speciesScientificName;

    private String type;

    private String platform;

    private URI projectUrl;

    @Before
    public void setUp() throws Exception {
        projectClientMock = projectId -> new EnaProject(projectId, EVA_STUDY_PUBLICATION_DATE);
        preEvaProjectClientMock = projectId -> new EnaProject(projectId, PRE_EVA_STUDY_PUBLICATION_DATE);

        // Variant Study fields
        studyId = "S1";
        studyName = "Study 1";
        studyDescription = "This is the study 1";
        center = "EBI";
        speciesScientificName = "Homo sapiens";
        projectUrl = new URI("http://www.study1.org");
        platform = "Illumina";
        type = "Case-Control";

        variantStudy = new VariantStudy(studyId, studyName, studyDescription, center, speciesScientificName, projectUrl,
                                        platform, type);
    }

    @Test
    public void transform() throws Exception {
        EvaStudyTransformer studyTransformer = new EvaStudyTransformer(projectClientMock);

        Database database = studyTransformer.transform(variantStudy);

        assertEquals("EVA", database.getName());
        assertEquals(LocalDate.now().toString(), database.getRelease());
        assertEquals(LocalDate.now().toString(), database.getReleaseDate());
        assertEquals(1, database.getEntryCount().intValue());

        Entry entry = database.getEntries().getEntry().get(0);
        assertEquals(studyId, entry.getId());
        assertEquals(studyName, entry.getName().getValue());
        assertEquals(studyDescription, entry.getDescription());
        assertEquals(center, entry.getAuthors());
        assertEquals(EVA_STUDY_PUBLICATION_DATE, entry.getDates().getDateByKey(PUBLICATION_DATE).getValue());

        AdditionalFields additionalFields = entry.getAdditionalFields();
        List<Field> fields = additionalFields.getField();
        assertFieldsContainsAttribute(fields, SPECIES, speciesScientificName);
        assertFieldsContainsAttribute(fields, FULL_DATASET_LINK, projectUrl.toString());
        assertFieldsContainsAttribute(fields, INSTRUMENT_PLATFORM, platform);
        assertFieldsContainsAttribute(fields, TECHNOLOGY_TYPE, type);
        // TODO: publications
    }

    @Test
    public void transformProjectWithOldPublicationDate() throws Exception {
        EvaStudyTransformer studyTransformer = new EvaStudyTransformer(preEvaProjectClientMock);

        Database database = studyTransformer.transform(variantStudy);
        Entry entry = database.getEntries().getEntry().get(0);

        assertEquals(EVA_FIRST_PUBLISHED_DATE, entry.getDates().getDateByKey(PUBLICATION_DATE).getValue());
    }

    private void assertFieldsContainsAttribute(List<Field> fields, String name, final String value) {
        assertTrue(fields.stream().anyMatch(field -> field.getName().equals(name) && field.getValue().equals(value)));
    }

}
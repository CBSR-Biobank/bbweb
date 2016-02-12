/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  //anatomicalSourceType.$inject = [];

  /**
   *
   */
  function AnatomicalSourceType() {
    var ALL_VALUES = [
        BLOOD(),
        BRAIN(),
        COLON(),
        KIDNEY(),
        ASCENDING_COLON(),
        DESCENDING_COLON(),
        TRANSVERSE_COLON(),
        DUODENUM(),
        HAIR(),
        ILEUM(),
        JEJENUM(),
        STOMACH_ANTRUM(),
        STOMACH_BODY(),
        STOOL(),
        TOE_NAILS(),
        URINE()
      ];

    var service = {
      BLOOD:            BLOOD,
      BRAIN:            BRAIN,
      COLON:            COLON,
      KIDNEY:           KIDNEY,
      ASCENDING_COLON:  ASCENDING_COLON,
      DESCENDING_COLON: DESCENDING_COLON,
      TRANSVERSE_COLON: TRANSVERSE_COLON,
      DUODENUM:         DUODENUM,
      HAIR:             HAIR,
      ILEUM:            ILEUM,
      JEJENUM:          JEJENUM,
      STOMACH_ANTRUM:   STOMACH_ANTRUM,
      STOMACH_BODY:     STOMACH_BODY,
      STOOL:            STOOL,
      TOE_NAILS:        TOE_NAILS,
      URINE:            URINE,
      values:           values
    };
    return service;

    //-------

    function BLOOD()            { return 'Blood'; }
    function BRAIN()            { return 'Brain'; }
    function COLON()            { return 'Colon'; }
    function KIDNEY()           { return 'Kidney'; }
    function ASCENDING_COLON()  { return 'Ascending Colon'; }
    function DESCENDING_COLON() { return 'Descending Colon'; }
    function TRANSVERSE_COLON() { return 'Transverse Colon'; }
    function DUODENUM()         { return 'Duodenum'; }
    function HAIR()             { return 'Hair'; }
    function ILEUM()            { return 'Ileum'; }
    function JEJENUM()          { return 'Jejenum'; }
    function STOMACH_ANTRUM()   { return 'Stomach Antrum'; }
    function STOMACH_BODY()     { return 'Stomach Body'; }
    function STOOL()            { return 'Stool'; }
    function TOE_NAILS()        { return 'Toe Nails'; }
    function URINE()            { return 'Urine'; }

    function values()           { return ALL_VALUES; }
  }

  return AnatomicalSourceType;
});

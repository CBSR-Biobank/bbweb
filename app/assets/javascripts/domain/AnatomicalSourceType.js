define([], function() {
  'use strict';

  //anatomicalSourceType.$inject = [];

  /**
   *
   */
  function anatomicalSourceType() {
    var ALL_VALUES = [
        BLOOD(),
        BRAIN(),
        COLON(),
        KIDNEY(),
        COLON_ASCENDING(),
        COLON_DESCENDING(),
        COLON_TRANSVERSE(),
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
      COLON_ASCENDING:  COLON_ASCENDING,
      COLON_DESCENDING: COLON_DESCENDING,
      COLON_TRANSVERSE: COLON_TRANSVERSE,
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
    function COLON_ASCENDING()  { return 'Ascending Colon'; }
    function COLON_DESCENDING() { return 'Descending Colon'; }
    function COLON_TRANSVERSE() { return 'Transverse Colon'; }
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

  return anatomicalSourceType;
});

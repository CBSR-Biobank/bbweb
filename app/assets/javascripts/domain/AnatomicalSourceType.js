/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function () {
  'use strict';

  /**
   * Anatomical sources where a specimen is collected from.
   * @enum {string}
   * @memberOf domain
   */
  var AnatomicalSourceType = {
    BLOOD:            'Blood',
    BRAIN:            'Brain',
    COLON:            'Colon',
    KIDNEY:           'Kidney',
    ASCENDING_COLON:  'Ascending Colon',
    DESCENDING_COLON: 'Descending Colon',
    TRANSVERSE_COLON: 'Transverse Colon',
    DUODENUM:         'Duodenum',
    HAIR:             'Hair',
    ILEUM:            'Ileum',
    JEJENUM:          'Jejenum',
    STOMACH_ANTRUM:   'Stomach Antrum',
    STOMACH_BODY:     'Stomach Body',
    STOOL:            'Stool',
    TOE_NAILS:        'Toe Nails',
    URINE:            'Urine'
  };

  return AnatomicalSourceType;
});

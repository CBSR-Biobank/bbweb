/**
 * The Specimen collection module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define(function(require) {
  'use strict';

  var angular                       = require('angular'),
      CollectionCtrl                = require('./CollectionCtrl'),
      CollectionEventChooseTypeCtrl = require('./CollectionEventChooseTypeCtrl'),
      CollectionEventDetailsCtrl    = require('./CollectionEventDetailsCtrl'),
      CollectionEventEditCtrl       = require('./CollectionEventEditCtrl'),
      CollectionStudyCtrl           = require('./CollectionStudyCtrl'),
      ParticipantCtrl               = require('./ParticipantCtrl'),
      ParticipantSummaryTabCtrl     = require('./ParticipantSummaryTabCtrl'),
      ParticipantCeventsTabCtrl     = require('./ParticipantCeventsTabCtrl'),
      ParticipantEditCtrl           = require('./ParticipantEditCtrl'),
      selectStudyDirective          = require('./selectStudyDirective'),
      states                        = require('./states');

  var module = angular.module('biobank.collection', []);

  module.controller('CollectionCtrl', CollectionCtrl);
  module.controller('CollectionStudyCtrl', CollectionStudyCtrl);

  module.controller('CollectionEventChooseTypeCtrl', CollectionEventChooseTypeCtrl);
  module.controller('CollectionEventDetailsCtrl', CollectionEventDetailsCtrl);
  module.controller('CollectionEventEditCtrl', CollectionEventEditCtrl);

  module.controller('ParticipantCtrl', ParticipantCtrl);
  module.controller('ParticipantSummaryTabCtrl', ParticipantSummaryTabCtrl);
  module.controller('ParticipantCeventsTabCtrl', ParticipantCeventsTabCtrl);
  module.controller('ParticipantEditCtrl', ParticipantEditCtrl);

  module.directive('selectStudy', selectStudyDirective);

  module.config(states);

  return module;
});

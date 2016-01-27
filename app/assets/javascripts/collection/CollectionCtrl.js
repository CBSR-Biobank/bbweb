/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2015 Canadian BioSample Repository (CBSR)
 */
define([], function() {
   'use strict';

   CollectionCtrl.$inject = ['Study', 'studyCounts', '$state'];

   /**
    *
    */
   function CollectionCtrl(Study, studyCounts) {
      var vm = this;

      vm.studyCounts = studyCounts;
      vm.haveEnabledStudies = (studyCounts.enabledCount > 0);
      vm.updateEnabledStudies = updateEnabledStudies;
      vm.getEnabledStudiesPanelHeader = getEnabledStudiesPanelHeader;

      //---

      function updateEnabledStudies(options) {
         return Study.list(options);
      }

      function getEnabledStudiesPanelHeader() {
         return 'Studies you participate in';
      }

   }

   return CollectionCtrl;
});

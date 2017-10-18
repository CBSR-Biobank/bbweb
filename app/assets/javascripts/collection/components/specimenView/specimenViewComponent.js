/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

import _ from 'lodash'

var component = {
  template: require('./specimenView.html'),
  controller: SpecimenViewController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    participant:         '<',
    collectionEventType: '<',
    collectionEvent:     '<',
    specimen:            '<'
  }
};

/*
 * Displays the details for a single specimen and also allows the user to update certain fields.
 */
/* @ngInject */
function SpecimenViewController($state, gettextCatalog, breadcrumbService, specimenStateLabelService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.specimenDescription = _.find(vm.collectionEventType.specimenDescriptions,
                                    { id: vm.specimen.specimenDescriptionId });

    vm.editParticipant = editParticipant;
    vm.back            = back;

    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.collection'),
      breadcrumbService.forStateWithFunc(
        'home.collection.study',
        function () { return vm.study.name; }),
      breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents',
        function () {
          return gettextCatalog.getString('Participant {{uniqueId}}',
                                          { uniqueId: vm.participant.uniqueId });
        }),
      breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents.details',
        function () {
          return gettextCatalog.getString('Visit # {{vnumber}}',
                                          { vnumber: vm.collectionEvent.visitNumber });
        }),
      breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents.details.specimen',
        function () { return vm.specimen.inventoryId; })
    ];

    vm.stateLabelFunc  = specimenStateLabelService.stateToLabelFunc(vm.specimen.state);
  }

  function editParticipant() {
    console.log(vm.participant);
  }

  function back() {
    $state.go('^');
  }
}

export default ngModule => ngModule.component('specimenView', component)

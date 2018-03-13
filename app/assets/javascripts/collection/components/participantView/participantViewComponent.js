/**
 * AngularJS Component for {@link domain.participants.Specimen Specimen} collection.
 *
 * @namespace collection.components.participantView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
function ParticipantViewController($window,
                                   $scope,
                                   $state,
                                   gettextCatalog,
                                   breadcrumbService) {
  var vm = this;
  vm.$onInit = onInit;

  //--

  function onInit() {
    vm.breadcrumbs = [
      breadcrumbService.forState('home'),
      breadcrumbService.forState('home.collection'),
      breadcrumbService.forStateWithFunc('home.collection.study', function () {
        return vm.study.name;
      }),
      breadcrumbService.forStateWithFunc('home.collection.study.participant', function () {
        return gettextCatalog.getString('Participant {{uniqueId}}',
                                        { uniqueId: vm.participant.uniqueId });
      })
    ];

    vm.tabs = [
      {
        heading: gettextCatalog.getString('Summary'),
        sref: 'home.collection.study.participant.summary',
        active: false
      },
      {
        heading: gettextCatalog.getString('Collection'),
        sref: 'home.collection.study.participant.cevents',
        active: false
      }
    ];
    vm.tabs.forEach((tab, index) => {
      tab.active = ($state.current.name.indexOf(tab.sref) >= 0);
      if (tab.active) {
        vm.active = index;
      }
    });
  }

}

/**
 * An AngularJS component that displays information for a {@link domain.participants.Participant Participant}.
 *
 * @memberOf collection.components.participantView
 *
 * @param {domain.studies.Study} study - The study the *Participant* belongs to.
 *
 * @param {domain.participants.Participant} participant - The participant to display information for.
 */
const participantViewComponent = {
  template: require('./participantView.html'),
  controller: ParticipantViewController,
  controllerAs: 'vm',
  bindings: {
    study:       '<',
    participant: '<'
  }
};

export default ngModule => ngModule.component('participantView', participantViewComponent)

/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
/* @ngInject */
class CeventGetTypeController {
  constructor($state,
              CollectionEventTypeName,
              CollectionEvent,
              gettextCatalog) {
    'ngInject'
    Object.assign(this, {
      $state,
      CollectionEventTypeName,
      CollectionEvent,
      gettextCatalog
    })
  }

  //--

  $onInit() {
    this.title = this.gettextCatalog.getString('Participant {{id}}: Add collection event',
                                               { id: this.participant.uniqueId })
    this.CollectionEventTypeName.list(this.study.id).then((reply) => {
      this.collectionEventTypeNames = reply
    })
  }

  updateCollectionEventType() {
    this.$state.go('home.collection.study.participant.cevents.add.details', { eventTypeId: this.eventTypeId })
  }
}

var component = {
  template: require('./ceventGetType.html'),
  controller: CeventGetTypeController,
  controllerAs: 'vm',
  bindings: {
    study:       '<',
    participant: '<'
  }
}

export default ngModule => ngModule.component('ceventGetType', component)

/**
 * AngularJS Component for {@link domain.participants.Specimen Specimen} collection.
 *
 * @namespace collection.components.specimenView
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Displays the details for a single specimen and also allows the user to update certain fields.
 */
/* @ngInject */
class SpecimenViewController {
  constructor($state,
              gettextCatalog,
              breadcrumbService,
              specimenStateLabelService,
              CollectionEventType,
              resourceErrorService) {
    Object.assign(this, {
      $state,
      gettextCatalog,
      breadcrumbService,
      specimenStateLabelService,
      CollectionEventType,
      resourceErrorService
    })
  }

  $onInit() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.collection'),
      this.breadcrumbService.forStateWithFunc('home.collection.study', () => this.study.name),
      this.breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents',
        () => this.gettextCatalog.getString('Participant {{uniqueId}}',
                                           { uniqueId: this.participant.uniqueId })),
      this.breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents.details',
        () => this.gettextCatalog.getString('Visit # {{vnumber}}',
                                           { vnumber: this.collectionEvent.visitNumber })),
      this.breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents.details.specimen',
        () => this.specimen.inventoryId)
    ];

    this.stateLabelFunc  = this.specimenStateLabelService.stateToLabelFunc(this.specimen.state);
  }

  editParticipant() {
    console.log(this.participant);
  }

  back() {
    this.$state.go('^');
  }
}

/**
 * An AngularJS component that lets the user view a {@link domain.participants.Specimen Specimen's}
 * information.
 *
 * @memberOf collection.components.specimenView
 *
 * @param {domain.studies.Study} study - The study the *Specimen* belongs to.
 *
 * @param {domain.participants.Participant} participant - The participant the *Specimen* belongs to.
 *
 * @param {domain.participants.CollectionEvent} collectionEvenType - the *Collection Event* the specimen
 * belongs to.
 *
 * @param {domain.participants.Specimen} specimen - the specimen to view.
 */
const specimenViewComponent = {
  template: require('./specimenView.html'),
  controller: SpecimenViewController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    participant:         '<',
    collectionEvent:     '<',
    specimen:            '<'
  }
};

export default ngModule => ngModule.component('specimenView', specimenViewComponent)

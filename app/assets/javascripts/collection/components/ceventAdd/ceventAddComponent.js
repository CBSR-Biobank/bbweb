/**
 * AngularJS Component for {@link domain.participants.CollectionEvent CollectionEvents}.
 *
 * @namespace collection.components.ceventAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class CeventAddController {

  constructor($state,
              gettextCatalog,
              modalService,
              notificationsService,
              domainNotificationService,
              timeService,
              CollectionEventType,
              CollectionEvent,
              breadcrumbService) {
    'ngInject'
    Object.assign(this, {
      $state,
      gettextCatalog,
      modalService,
      notificationsService,
      domainNotificationService,
      timeService,
      CollectionEventType,
      CollectionEvent,
      breadcrumbService
    })
  }

  $onInit() {
    this.collectionEvent = new this.CollectionEvent({ participantId: this.participant.id },
                                                    this.collectionEventType);
    this.title = this.gettextCatalog.getString('Participant {{id}}: Add collection event',
                                               { id: this.participant.uniqueId });
    this.timeCompleted = new Date();
    this.configureBreadcrumbs();
  }

  configureBreadcrumbs() {
    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.collection'),
      this.breadcrumbService.forStateWithFunc('home.collection.study', () => this.study.name),
      this.breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents',
        () => this.gettextCatalog.getString('Participant {{uniqueId}}',
                                           { uniqueId: this.participant.uniqueId })),
      this.breadcrumbService.forStateWithFunc(
        'home.collection.study.participant.cevents.add',
        () => this.gettextCatalog.getString('Event type: {{type}}',
                                           { type: this.collectionEventType.name }))
    ];
  }

  submit() {
    this.collectionEvent.timeCompleted = this.timeService.dateAndTimeToUtcString(this.timeCompleted);
    this.collectionEvent.add()
      .then(cevent => {
        this.notificationsService.submitSuccess();
        this.$state.go('home.collection.study.participant.cevents.details',
                       { visitNumber: cevent.visitNumber },
                       { reload: true });
      })
      .catch(error => {
        const entityName = this.gettextCatalog.getString('Event')
        if (error.message
            .match(/EntityCriteriaError: a collection event with this visit number already exists/)) {

          this.modalService.modalOk(
            this.gettextCatalog.getString('Visit number error'),
            this.gettextCatalog.getString(
              `An event with visit # <b>{{visitNumber}}</b> already exists.
               Please use another visit number.`,
              { visitNumber: this.collectionEvent.visitNumber }))
        } else {
          this.domainNotificationService.updateErrorModal(error, entityName)
            .catch(() => {
              this.$state.go('home.collection.study.participant', { participantId: this.participant.id });
            });
        }
      });
  }

  cancel() {
    this.$state.go('home.collection.study.participant.cevents');
  }

  dateTimeOnEdit(datetime) {
    this.timeCompleted = datetime;
  }
}

/**
 * An AngularJS component that lets the user add a {@link domain.participants.CollectionEvent
 * CollectionEvent}.
 *
 * @memberOf collection.components.ceventAdd
 *
 * @param {domain.studies.Study} study - The study to add to this *Collection Event* to.
 *
 * @param {domain.participants.Participant} participant - The participant to add to this *Collection Event*
 * to.
 *
 * @param {domain.studies.CollectionEventType} collectionEventType - the *Collection Event Type* for this
 * *Collection Event*.
 */
const ceventAddComponent = {
  template: require('./ceventAdd.html'),
  controller: CeventAddController,
  controllerAs: 'vm',
  bindings: {
    study:               '<',
    participant:         '<',
    collectionEventType: '<'
  }
};

export default ngModule => ngModule.component('ceventAdd', ceventAddComponent)

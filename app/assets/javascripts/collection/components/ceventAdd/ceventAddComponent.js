/**
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2016 Canadian BioSample Repository (CBSR)
 */

/*
 * Controller for this component.
 */
class CeventAddController {

  constructor($state,
              gettextCatalog,
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
      notificationsService,
      domainNotificationService,
      timeService,
      CollectionEventType,
      CollectionEvent,
      breadcrumbService
    })
  }

  $onInit() {
    this.CollectionEventType.get(this.collectionEventTypeId)
      .then((collectionEventType) => {
        this.collectionEventType = collectionEventType;
        this.configureBreadcrumbs();

        this.collectionEvent = new this.CollectionEvent({ participantId: this.participant.id },
                                                        this.collectionEventType);

        this.title = this.gettextCatalog.getString(
          'Participant {{id}}: Add collection event', { id: this.participant.uniqueId });
        this.timeCompleted = new Date();
      })
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
                       { collectionEventId: cevent.id },
                       { reload: true });
      })
      .catch(error => {
        this.domainNotificationService.updateErrorModal(error, this.gettextCatalog.getString('collectionEvent'))
          .catch(() => {
            this.$state.go('home.collection.study.participant', { participantId: this.participant.id });
          });
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
 * Used to add a collection event.
 */
const component = {
  template: require('./ceventAdd.html'),
  controller: CeventAddController,
  controllerAs: 'vm',
  bindings: {
    study:                 '<',
    participant:           '<',
    collectionEventTypeId: '@'
  }
};

export default ngModule => ngModule.component('ceventAdd', component)

/**
 * AngularJS Component for {@link domain.studies.CollectionEventType CollectionEventType} administration.
 *
 * @namespace admin.studies.components.ceventTypeAdd
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

class CeventTypeAddController {

  constructor($state,
              gettextCatalog,
              CollectionEventType,
              breadcrumbService,
              domainNotificationService,
              notificationsService) {
    'ngInject';
    Object.assign(this,
                  {
                    $state,
                    gettextCatalog,
                    CollectionEventType,
                    breadcrumbService,
                    domainNotificationService,
                    notificationsService
                  });
  }

  $onInit() {
    this.ceventType  = new this.CollectionEventType({}, { study: this.study });
    this.returnState = 'home.admin.studies.study.collection';

    this.breadcrumbs = [
      this.breadcrumbService.forState('home'),
      this.breadcrumbService.forState('home.admin'),
      this.breadcrumbService.forState('home.admin.studies'),
      this.breadcrumbService.forStateWithFunc(
        `home.admin.studies.study.collection({ studySlug: "${this.study.slug}" })`,
        () => this.study.name),
      this.breadcrumbService.forStateWithFunc(
        'home.admin.studies.study.collection.ceventTypeAdd',
        () => this.gettextCatalog.getString('Add collection event'))
    ];
  }

  submit() {
    this.ceventType.add().then(submitSuccess).catch(submitError);

    function submitSuccess() {
      this.notificationsService.submitSuccess();
      return this.$state.go(this.returnState, {}, { reload: true });
    }

    function submitError(error) {
      this.domainNotificationService.updateErrorModal(
        error,
        this.gettextCatalog.getString('collection event type'));
    }
  }

  cancel() {
    return this.$state.go(this.returnState);
  }

}

/**
 * An AngularJS component that allows the user to add a {@link domain.studies.CollectionEventType
 * CollectionEventType} using an HTML form.
 *
 * @memberOf admin.studies.components.ceventTypeAdd
 *
 * @param {domain.studies.Study} study - the study to add the collection event type to.
 */
const ceventTypeAddComponent = {
  template: require('./ceventTypeAdd.html'),
  controller: CeventTypeAddController,
  controllerAs: 'vm',
  bindings: {
    study: '<'
  }
};

export default ngModule => ngModule.component('ceventTypeAdd', ceventTypeAddComponent)

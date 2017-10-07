/**
 * Study module.
 *
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2017 Canadian BioSample Repository (CBSR)
 */
import AdminStudiesAnnotationTypesModule from './modules/annotationTypes';
import UsersModule                       from '../../users';
import angular                           from 'angular';
import collectionSpecimenDescriptionAdd  from './components/collectionSpecimenDescriptionAdd/collectionSpecimenDescriptionAddComponent';
import studiesPagedList                  from './components/studiesPagedList/studiesPagedListComponent';
// import processingDirecitves      from './modules/processing');
// import processing                from './processing');
// import specimenGroups            from './specimenGroups');

const AdminStudiesModule = angular.module('biobank.admin.studies',
                                                 [
                                                   UsersModule,
                                                   AdminStudiesAnnotationTypesModule
                                                 ])
  .component('studiesPagedList',        studiesPagedList)
  .component('ceventTypeAdd',           require('./components/ceventTypeAdd/ceventTypeAddComponent'))
  .component('ceventTypeView',          require('./components/ceventTypeView/ceventTypeViewComponent'))
  .component('ceventTypesAddAndSelect',
             require('./components/ceventTypesAddAndSelect/ceventTypesAddAndSelectComponent'))
  .component('collectionSpecimenDescriptionAdd', collectionSpecimenDescriptionAdd)
  .component('collectionSpecimenDescriptionSummary',
             require('./components/collectionSpecimenDescriptionSummary/collectionSpecimenDescriptionSummaryComponent'))
  .component('collectionSpecimenDescriptionView',
             require('./components/collectionSpecimenDescriptionView/collectionSpecimenDescriptionViewComponent'))
  .component('studyCollection',         require('./components/studyCollection/studyCollectionComponent'))
  .component('studiesAdmin',            require('./components/studiesAdmin/studiesAdminComponent'))
  .component('studyAdd',                require('./components/studyAdd/studyAddComponent'))
  .component('studyParticipantsTab',    require('./components/studyParticipantsTab/studyParticipantsTabComponent'))
  .component('studySummary',            require('./components/studySummary/studySummaryComponent'))
  .component('studyView',               require('./components/studyView/studyViewComponent'))
  .component('studyNotDisabledWarning',
             require('./components/studyNotDisabledWarning/studyNotDisabledWarningComponent'))
  .component('studyProcessingTab',
             require('./components/studyProcessingTab/studyProcessingTabComponent'))

  .config(require('./states'))
  .config(require('./ceventTypes/states'))
  .config(require('./participants/states'))
  .name;

export default AdminStudiesModule;

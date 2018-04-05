/*
 * @author Nelson Loyola <loyola@ualberta.ca>
 * @copyright 2018 Canadian BioSample Repository (CBSR)
 */

import { LabelService } from '../../../base/services/LabelService';

/**
 * An AngularJS service that converts a StudyState to a i18n string that can
 * be displayed to the study.
 *
 * @memberof studies.services
 */
class StudyStateLabelService extends LabelService {

  /**
   * @param {base.BbwebError} BbwebError - AngularJS factory for exceptions.
   *
   * @param {object} StudyState - AngularJS constant that enumerates all the study states.
   *
   * @param {object} gettextCatalog - The service that allows strings to be translated to other languages.
   */
  constructor(BbwebError, StudyState, gettextCatalog) {
    'ngInject';

    super(BbwebError,
          [
            { id: StudyState.DISABLED, label: () => gettextCatalog.getString('Disabled') },
            { id: StudyState.ENABLED,  label: () => gettextCatalog.getString('Enabled') },
            { id: StudyState.RETIRED,  label: () => gettextCatalog.getString('Retired') },
          ]);
    Object.assign(this, { StudyState, gettextCatalog });
  }

  /**
   * Returns the function that should be called to display the label for a {@link domain.studies.StudyState
   * StudyState} state.
   *
   * @param {domain.studies.StudyState} state - the state to get a function for.
   *
   * @return {function} a function that returns a label that can be displayed to the user.
   */
  stateToLabelFunc(state) {
    return this.getLabel(state);
  }

}

export default ngModule => ngModule.service('studyStateLabelService', StudyStateLabelService)

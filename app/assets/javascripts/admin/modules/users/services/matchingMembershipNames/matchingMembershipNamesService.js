/**
 * This AngularJS Service allows the user to select a {@link domain.access.MembershipName MembershipName} from
 * a modal that contains a `uib-typeahead`.
 *
 * @memberOf ng.admin.users.services
 */
class MatchingMembershipNamesService {

  constructor(asyncInputModal, MembershipName) {
    'ngInject'
    Object.assign(this, { asyncInputModal, MembershipName })
  }

  /**
   * Opens a modal dialog that prompts the user to type the name, or partial name, for a membership.
   *
   * <p>Matches for the text entered by the user are then retrieved asynchronously from the server, and the
   * matches are displayed in a Bootstrap Typeahead input (`uib-typeahead`).
   *
   * <p>If the user presses the **OK** button, the object returned by the modal is the object corresponding
   * to the label selected by the user.
   *
   * @param {string} title - the title to display for the modal diaglog box.
   *
   * @param {string} prompt - the prompt to display next to the input field.
   *
   * @param {string} placeholder - a message to display in the input field when no value is present.
   *
   * @param {string} noMatchesMessage - the message to display if the input does not match any role names.
   *
   * @param {Array<domain.access.RoleName>} omit - an array of role names to omit.
   *
   * @return {Promise<domain.access.MemberhipName>} If the user presses the **OK** button, the membership name
   * selected by the user. Otherwise, a rejected promise is returned.
   */
  open(title, prompt, placeholder, noMatchesMessage, namesToOmit) {
    const getMatchingMembershipNames = viewValue => {
      let filter = 'name:like:' + viewValue
      if (namesToOmit.length > 0) {
        filter += ';name:out:' + namesToOmit.join(',')
      }

      return this.MembershipName.list({ filter })
        .then(names => names.map(m => ({ label: m.name, obj: m })))
    }

    return this.asyncInputModal
      .open(title, prompt, placeholder, noMatchesMessage, getMatchingMembershipNames)
      .result;
  }

}

export default ngModule => ngModule.service('matchingMembershipNames', MatchingMembershipNamesService)
